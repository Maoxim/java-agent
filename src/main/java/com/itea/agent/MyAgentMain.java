package com.itea.agent;

import com.google.gson.Gson;
import com.itea.mutationtest.MutationContext;
import com.itea.transformer.NullTransformer;
import com.itea.transformer.ReturnValsTransformer;
import com.itea.vo.MutatorVo;
import com.itea.vo.ResponseVo;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.config.Mutator;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MyAgentMain {
    //agent启动计时
    private static final long ts = System.nanoTime();
    //todo 打算写日志，但会classNotFound，改为自定义类
    private static final Logger logger = Logger.getLogger();

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        main(agentArgs, instrumentation);
    }

    private static synchronized void main(String args, Instrumentation inst) {
        try {
            Thread agentThread = new Thread(
                    () -> {
                        try {
                            startServer(inst);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            //todo
                            logger.info("thread over...");
                        }
                    });
            try {
                //设置为守护线程
                //用户线程和守护线程的区别：
                //1. 主线程结束后用户线程还会继续运行,JVM存活；主线程结束后守护线程和JVM的状态又下面第2条确定。
                //2.如果没有用户线程，都是守护线程，那么JVM结束（随之而来的是所有的一切烟消云散，包括所有的守护线程）。
                agentThread.setDaemon(true);
                logger.info("starting agent thread");
                agentThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //todo
            }
        } finally {
            //todo
            logger.info("Agent init took: " + (System.nanoTime() - ts) + "ns");
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private static void startServer(Instrumentation instrumentation) {
        ServerSocket ss;
        try {
            ss = new ServerSocket(12345);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while (true) {
            try {
                Socket sock = ss.accept();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
                String line = null;
                logger.info("listening port: " + 12345);
                line = bufferedReader.readLine();

                //将收到的数据转为json格式
                Gson gson = new Gson();
                MutatorVo mutatorVo = gson.fromJson(line, MutatorVo.class);
                logger.info("mutationInfo=" + mutatorVo);
                //发json格式的给client
                ResponseVo responseVo = new ResponseVo();
                responseVo.setMsg("mutationInfo=" + mutatorVo);
                // 调用Gson的String toJson(Object)方法将Bean转换为json字符串
                String pJson = gson.toJson(responseVo);
                writer.write(pJson);
                writer.newLine();
                writer.flush();

                List<Class<?>> targetClasses = findInstrumentClasses(instrumentation.getAllLoadedClasses(), getClassToMutate(mutatorVo));
                for (Class<?> clazz : targetClasses) {
                    logger.info("begin retransform " + clazz.getName());
                    List<String> mutators = getMutators(mutatorVo);
                    logger.info("mutators are " + mutators);
                    MutationContext context = new MutationContext(clazz.getName(), mutators);

                    saveOriginClassFile(instrumentation, clazz, context);

                    GregorMutater gregorMutater = new GregorMutater(context.getOriginClassFileBuffer(), Collections.emptyList(), Mutator.fromStrings(mutators));
                    List<MutationDetails> mutations = gregorMutater.findMutations(clazz.getName());
                    logger.info("mutations size is: " + mutations.size());

                    if (mutations.size() == 0) {
                        sendMsgToClient("{\"returnCode\":\"1\", \"msg\":\" not found mutations in class "
                                + clazz.getName()
                                + " with mutators "
                                + mutators + "\"}");
                    }

                    for (MutationDetails mutationDetails : mutations) {

                        //打印一下mutationDetails
                        logger.info("details = "+mutationDetails.toString());

                        byte[] mutation = gregorMutater.getMutation(mutationDetails.getId());
                        ClassFileTransformer returnValTransformer = initMutatingTransformer(context, mutationDetails.getMutator(), mutation);
                        instrumentation.addTransformer(returnValTransformer, true);
                        logger.info("retransform " + clazz.getName() + ", inject mutation:" + mutationDetails);
                        instrumentation.retransformClasses(clazz);
                        instrumentation.removeTransformer(returnValTransformer);
                        logger.info("retransform done, waiting client response......");

                        responseVo.setMsg("已完成变异注入，等待客户端相应");
                        pJson = gson.toJson(responseVo);
                        writer.write(pJson);
                        writer.newLine();
                        writer.flush();

                        //todo 等待客户端响应
                        while (true) {
                            String clientResponse = bufferedReader.readLine();
                            if (clientResponse.equals("TestDone")) {
                                context.setGetMutations(false);
                                logger.info("recover classfile");
                                ClassFileTransformer recoverClassTransformer = initMutatingTransformer(context, mutationDetails.getMutator(), context.getOriginClassFileBuffer());
                                instrumentation.addTransformer(recoverClassTransformer, true);
                                instrumentation.retransformClasses(clazz);
                                instrumentation.removeTransformer(recoverClassTransformer);
                                logger.info("classfile " + clazz.getName() + " is recovered");
                                break;
                            }
                        }
                    }
                }

                responseVo.setMsg("TEST END");
                pJson = gson.toJson(responseVo);
                writer.write(pJson);
                writer.newLine();
                writer.flush();
                bufferedReader.close();
            } catch (RuntimeException | IOException re) {
                logger.debug(re.getMessage());
            } catch (UnmodifiableClassException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendMsgToClient(String msg) {

    }

    private static String getMsgFromClient() {
        return "";
    }

    private static List<Class<?>> findInstrumentClasses(Class<?>[] allLoadedClasses, String classToMutate) {
        List<Class<?>> targetClasses = new ArrayList<>();
        for (Class<?> clazz : allLoadedClasses) {
            if (clazz.getName().equals(classToMutate)) {
                targetClasses.add(clazz);
            }
        }
        return targetClasses;
    }

    private static void saveOriginClassFile(Instrumentation instrumentation, Class<?> clazz, MutationContext context) throws UnmodifiableClassException {
        NullTransformer transformer = new NullTransformer(context, clazz.getName());
        instrumentation.addTransformer(transformer, true);
        instrumentation.retransformClasses(clazz);
        instrumentation.removeTransformer(transformer);
    }

    private static ClassFileTransformer initMutatingTransformer(MutationContext context, String mutator, byte[] mutation) {
        switch (mutator) {
            case "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator":
                return new ReturnValsTransformer(context, mutator, mutation);
            case "ConditionBoundary":
                return null;
            default:
                throw new RuntimeException("Unknown mutator:" + mutator);
        }
    }

    /**
     * 功能:获取变异因子
     */
    private static List<String> getMutators(MutatorVo msg) {
        List<String>res = msg.getMutators();
        return res;
    }

    /**
     * 功能:获取全类名
     */
    private static String getClassToMutate(MutatorVo msg) {
        List<String>res = msg.getTargetClass();
        return  res.get(0);
    }
}
