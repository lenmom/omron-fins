package io.github.mookins.omron.fins.codec;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mookins.omron.fins.tcp.FinsTcpFrame;
import io.github.mookins.omron.fins.tcp.FinsTcpFrameBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

public class FinsTcpFrameCodec extends ByteToMessageCodec<FinsTcpFrame> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
	}


	@Override
	protected void encode(ChannelHandlerContext context, FinsTcpFrame finsTcpFrame, ByteBuf out) throws Exception {
		logger.debug("Encode FINS/TCP -> ByteBuf");
		logger.debug(finsTcpFrame.toString());

		out.writeBytes(finsTcpFrame.toByteArray());
	}
	
	@Override
	protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) throws Exception {
		logger.debug("Decode ByteBuf -> FINS/TCP");
		
		byte[] data = new byte[in.readableBytes()];
		in.readBytes(data);
		FinsTcpFrame finsTcpFrame = FinsTcpFrameBuilder.parseFrom(data);

		logger.debug(finsTcpFrame.toString());
		
		out.add(finsTcpFrame);
	}
}
