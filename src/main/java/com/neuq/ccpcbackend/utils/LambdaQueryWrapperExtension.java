package com.neuq.ccpcbackend.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import jakarta.annotation.Nonnull;

import java.util.List;

public class LambdaQueryWrapperExtension {
    public static <T, R> @Nonnull LambdaQueryWrapper<T> inIfNotEmpty(@Nonnull LambdaQueryWrapper<T> wrapper, @Nonnull SFunction<T, R> column, @Nonnull List<R> values) {
        if (!values.isEmpty()) {
            return wrapper.in(column, values);
        } else {
            return wrapper.and(it -> it.apply("1=0"));
        }
    }
}
