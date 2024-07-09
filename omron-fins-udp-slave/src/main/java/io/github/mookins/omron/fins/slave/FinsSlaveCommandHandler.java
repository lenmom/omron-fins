package io.github.mookins.omron.fins.slave;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mookins.omron.fins.FinsCommandCode;
import io.github.mookins.omron.fins.FinsFrame;
import io.github.mookins.omron.fins.FinsFrameBuilder;
import io.github.mookins.omron.fins.FinsIoMemoryArea;
import io.github.mookins.omron.fins.FinsMessageType;
import io.github.mookins.omron.fins.MemoryAreaWriteCommandHandler;
import io.github.mookins.omron.fins.commands.FinsMemoryAreaWriteCommand;
import io.github.mookins.omron.fins.commands.FinsMemoryAreaWriteWordCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class FinsSlaveCommandHandler extends SimpleChannelInboundHandler<FinsFrame> {

	private final static Logger logger = LoggerFactory.getLogger(FinsSlaveCommandHandler.class);

	private final FinsNettyUdpSlave finsNettyUdpSlave;

	public FinsSlaveCommandHandler(FinsNettyUdpSlave finsNettyUdpSlave) {
		this.finsNettyUdpSlave = finsNettyUdpSlave;
	}

	@Override
	public void channelRead0(ChannelHandlerContext context, FinsFrame finsFrame) throws Exception {
		logger.debug(String.format("FINS command handler = %s, ", finsFrame));
		
		// Peek at the command code and then send it to the correct handler
		ByteBuffer buf = ByteBuffer.wrap(finsFrame.getData());
		FinsCommandCode commandCode = FinsCommandCode.valueOf(buf.getShort()).get();
		
		FinsFrame responseFinsFrame = FinsFrameBuilder.builderFromPrototype(finsFrame)
			.setMessageType(FinsMessageType.RESPONSE)
			.setDestinationAddress(finsFrame.getSourceAddress())
			.setSourceAddress(finsFrame.getDestinationAddress())
			.setData(new byte[]{0x01, 0x02, 0x00, 0x00})
			.build();
		
		switch (commandCode) {
			default:
				break;
			
			case MEMORY_AREA_WRITE:			
				this.finsNettyUdpSlave.getMemoryAreaWriteHandler()
					.ifPresent(handler -> handleMemoryAreaWriteCommand(handler, finsFrame));
				break;
			case MEMORY_AREA_READ:
				responseFinsFrame = FinsFrameBuilder.builderFromPrototype(finsFrame)
						.setMessageType(FinsMessageType.RESPONSE)
						.setDestinationAddress(finsFrame.getSourceAddress())
						.setSourceAddress(finsFrame.getDestinationAddress())
						.setData(new byte[]{0x01, 0x01, 0x00, 0x00,0x00,0x11})
						.build();
				break;
		}
		
		context.channel().writeAndFlush(responseFinsFrame);
	}
	
	private void handleMemoryAreaWriteCommand(MemoryAreaWriteCommandHandler handler, FinsFrame finsFrame) {
		Optional<FinsMemoryAreaWriteCommand> command = Optional.empty();
		
		FinsIoMemoryArea memoryAreaCode = FinsIoMemoryArea.valueOf(finsFrame.getData()[2]).get();
		switch(memoryAreaCode.getDataByteSize()) {
			// Unknown data type size, should never get here
			default:
				break;
			
			// Bit write
			case 1:
				break;
			
			// Word write
			case 2:
				command = Optional.of(FinsMemoryAreaWriteWordCommand.Builder.parseFrom(finsFrame.getData()));
				break;
				
			// Double word write
			case 4:
				break;
		}
			
		command.ifPresent(c -> handler.handle(c));
	}
	
}
