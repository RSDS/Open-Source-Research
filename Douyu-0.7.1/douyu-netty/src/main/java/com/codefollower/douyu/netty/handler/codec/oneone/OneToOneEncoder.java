/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.codefollower.douyu.netty.handler.codec.oneone;

import static com.codefollower.douyu.netty.channel.Channels.*;

import com.codefollower.douyu.netty.buffer.ChannelBuffers;
import com.codefollower.douyu.netty.channel.Channel;
import com.codefollower.douyu.netty.channel.ChannelDownstreamHandler;
import com.codefollower.douyu.netty.channel.ChannelEvent;
import com.codefollower.douyu.netty.channel.ChannelHandlerContext;
import com.codefollower.douyu.netty.channel.ChannelPipeline;
import com.codefollower.douyu.netty.channel.MessageEvent;
import com.codefollower.douyu.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import com.codefollower.douyu.netty.handler.codec.frame.Delimiters;

/**
 * Transforms a write request into another write request.  A typical setup for
 * TCP/IP would be:
 * <pre>
 * {@link ChannelPipeline} pipeline = ...;
 *
 * // Decoders
 * pipeline.addLast("frameDecoder", new {@link DelimiterBasedFrameDecoder}(80, {@link Delimiters#nulDelimiter()}));
 * pipeline.addLast("customDecoder", new {@link OneToOneDecoder}() { ... });
 *
 * // Encoder
 * pipeline.addLast("customEncoder", new {@link OneToOneEncoder}() { ... });
 * </pre>
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev:231 $, $Date:2008-06-12 16:44:50 +0900 (목, 12 6월 2008) $
 *
 * @apiviz.landmark
 */
public abstract class OneToOneEncoder implements ChannelDownstreamHandler {

    protected OneToOneEncoder() {
        super();
    }

    @Override
    public void handleDownstream(
            ChannelHandlerContext ctx, ChannelEvent evt) throws Exception {
        if (!(evt instanceof MessageEvent)) {
            ctx.sendDownstream(evt);
            return;
        }

        MessageEvent e = (MessageEvent) evt;
        Object originalMessage = e.getMessage();
        Object encodedMessage = encode(ctx, e.getChannel(), originalMessage);
        if (originalMessage == encodedMessage) {
            ctx.sendDownstream(evt);
        } else if (encodedMessage != null) {
            write(ctx, e.getFuture(), encodedMessage, e.getRemoteAddress());
        }
    }

    /**
     * Transforms the specified message into another message and return the
     * transformed message.  Note that you can not return {@code null}, unlike
     * you can in {@link OneToOneDecoder#decode(ChannelHandlerContext, Channel, Object)};
     * you must return something, at least {@link ChannelBuffers#EMPTY_BUFFER}.
     */
    protected abstract Object encode(
            ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception;
}