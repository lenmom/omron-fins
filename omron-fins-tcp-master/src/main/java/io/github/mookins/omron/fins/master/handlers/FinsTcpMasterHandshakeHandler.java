package io.github.mookins.omron.fins.master.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mookins.omron.fins.codec.FinsFrameCodec;
import io.github.mookins.omron.fins.master.FinsNettyTcpMaster;
import io.github.mookins.omron.fins.tcp.FinsTcpCommandCode;
import io.github.mookins.omron.fins.tcp.FinsTcpErrorCode;
import io.github.mookins.omron.fins.tcp.FinsTcpFrame;
import io.github.mookins.omron.fins.tcp.FinsTcpFrameBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.ByteBuffer;

public class FinsTcpMasterHandshakeHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(FinsTcpMasterHandshakeHandler.class);
    private final FinsNettyTcpMaster master;

    public FinsTcpMasterHandshakeHandler(final FinsNettyTcpMaster master) {
        this.master = master;
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        super.channelActive(context);
        logger.debug("Channel active, starting handshake");
        FinsTcpFrame finsTcpFrame = new FinsTcpFrameBuilder()
                .setCommandCode(FinsTcpCommandCode.FINS_CLIENT_NODE_ADDRESS_DATA_SEND)
                .setErrorCode(FinsTcpErrorCode.NORMAL)
                .setData(new byte[]{0, 0, 0, 0x0B})
                .build();
        context.writeAndFlush(finsTcpFrame);
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        if (message instanceof FinsTcpFrame) {
            logger.debug("We have received a FinsTcp frame during handshake");
            // TODO check that we have a legitimate handshake reply

            context.pipeline().remove(this.getClass());
            context.pipeline()
                    .addLast(new FinsFrameCodec())
                    .addLast(new FinsMasterHandler(this.master));

            this.master.getQueue().remove().complete(null);
        }
    }


}
