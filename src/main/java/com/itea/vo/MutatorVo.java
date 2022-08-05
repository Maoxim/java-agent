package com.itea.vo;

import java.io.Serializable;
import java.util.List;

/**
 * @description:数据交互模型
 * @author: oyyy
 * @date: 2022/8/3 18:30
 */
public class MutatorVo implements Serializable {

    private List<String> targetClass;
    private List<String> method;
    private List<String> mutators;

    public List<String> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(List<String> targetClass) {
        this.targetClass = targetClass;
    }

    public List<String> getMethod() {
        return method;
    }

    public void setMethod(List<String> method) {
        this.method = method;
    }

    public List<String> getMutators() {
        return mutators;
    }

    public void setMutators(List<String> mutators) {
        this.mutators = mutators;
    }

    public MutatorVo(List<String> targetClass, List<String> mutators) {
        this.targetClass = targetClass;
        this.mutators = mutators;
    }

    @Override
    public String toString() {
        return "MutatorVo{" +
                "targetClass=" + targetClass +
                ", method=" + method +
                ", mutators=" + mutators +
                '}';
    }

    public MutatorVo(List<String> targetClass, List<String> method, List<String> mutators) {
        this.targetClass = targetClass;
        this.method = method;
        this.mutators = mutators;
    }
}
