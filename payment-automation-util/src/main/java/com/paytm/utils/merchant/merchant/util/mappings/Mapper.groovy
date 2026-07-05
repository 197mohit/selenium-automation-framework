package com.paytm.utils.merchant.merchant.util.mappings

import com.paytm.utils.merchant.merchant.util.annotations.Weight

import java.lang.annotation.Annotation

trait Mapper implements Comparable<Mapper> {

    int compareTo(Mapper o) {
        int diff = this.size() - o.size()
        return diff == 0 ? 0 : diff / diff.abs() as int
    }

    int size() {
        { o -> (this as Map).collect { o[it.key] ? it.value : 0 }.sum() }(this)
    }

    boolean matches(Annotation annotation) {
        (this as Map).keySet().every {
            def map = ["true": true, "false": false, "any": null]
            def key = annotation."${it}"()
            def mVal = this."${it}"
            if (key instanceof Annotation) return mVal.matches(key)
            def aVal = map.containsKey(key) ? map[key] : key
            aVal == null ? true : aVal == mVal
        }
    }

    Object asType(Class<?> aClass) {
        if (aClass == Map) {
            return this.class.declaredFields.findAll { it.getAnnotation(Weight) }.collectEntries {
                [(it.name): (it.getAnnotation(Weight).value() ?: (this."${it.name}" instanceof Number ? this."${it.name}" : this."${it.name}".size()))]
            }
        } else return super.asType(aClass)
    }

}