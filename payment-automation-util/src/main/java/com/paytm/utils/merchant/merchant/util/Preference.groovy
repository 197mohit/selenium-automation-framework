package com.paytm.utils.merchant.merchant.util

class Preference {

    final String name
    final boolean enabled
    final boolean disabled

    Preference(String name) {
        this.name = name
    }

    protected Preference(name, enabled) {
        this.name = name
        this.enabled = enabled
        this.disabled = !enabled
    }

    @Override
    boolean equals(Object obj) {
        obj instanceof Preference && this.name == obj.name && this.enabled == obj.enabled
    }


    @Override
    String toString() {
        (!this.enabled ? '!' : '') + this.name
    }
}
