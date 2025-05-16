package com.github.javachaser.constants;

import lombok.Getter;

/**
 * @author 85949
 * @date 2025/5/7 17:08
 * @description
 */
public class CommonValue {

    @Getter
    public enum ResponseMode {
        BLOCKING("blocking") {
            @Override
            public boolean isEq(String origin) {
                return origin != null && origin.equals(getType());
            }
        },
        STREAMING("streaming") {
            @Override
            public boolean isEq(String origin) {
                return origin != null && origin.equals(getType());
            }
        };
        private final String type;

        ResponseMode(String type) {
            this.type = type;
        }

        public abstract boolean isEq(String origin);
    }

}
