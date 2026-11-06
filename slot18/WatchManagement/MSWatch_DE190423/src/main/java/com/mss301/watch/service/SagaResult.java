package com.mss301.watch.service;

// [TODO-6.13: kết quả kèm sagaId]
public record SagaResult<T>(String sagaId, T body) {
}
