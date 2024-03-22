/*
 * The MIT License (MIT) Copyright (c) 2020-2023 artipie.com
 * https://github.com/artipie/artipie/blob/master/LICENSE.txt
 */

package com.artipie.http.servlet;

import com.artipie.asto.Content;
import com.artipie.http.Connection;
import com.artipie.http.Headers;
import com.artipie.http.rs.RsStatus;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import org.cqfn.rio.WriteGreed;
import org.cqfn.rio.stream.ReactiveOutputStream;

/**
 * Connection implementation with servlet response as a back-end.
 * @since 0.18
 */
final class ServletConnection implements Connection {

    /**
     * Servlet response.
     */
    private final HttpServletResponse rsp;

    /**
     * New Artipie connection with servlet response back-end.
     * @param rsp Servlet response
     */
    ServletConnection(final HttpServletResponse rsp) {
        this.rsp = rsp;
    }

    @Override
    public CompletionStage<Void> accept(RsStatus status, Headers headers, Content body) {
        this.rsp.setStatus(status.code());
        headers.forEach(kv -> this.rsp.setHeader(kv.getKey(), kv.getValue()));
        try {
            return new ReactiveOutputStream(this.rsp.getOutputStream())
                .write(body, WriteGreed.SYSTEM.adaptive());
        } catch (final IOException iex) {
            final CompletableFuture<Void> failure = new CompletableFuture<>();
            failure.completeExceptionally(new CompletionException(iex));
            return failure;
        }
    }
}
