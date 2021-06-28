import type { FlockMonitorCommand, MessageDTO } from "./StreamDtos";
import { connectClient } from "./RSocket";
import { MessageHandler } from "./MessageHandler";

class EventBus {
  private isConnected: Boolean = false;
  private commandsBuffer: FlockMonitorCommand[] = [];
  private messageHandler: MessageHandler = new MessageHandler();
  private commandSink = this.createCommandSink();

  request(command: FlockMonitorCommand) {
    console.debug("Requesting command in state: " + this.isConnected);
    if (this.isConnected) {
      this.commandSink(command);
    } else {
      this.commandsBuffer.push(command);
    }
  }

  private handleMessage(message: MessageDTO) {
    this.messageHandler.handleMessage(message);
  }

  private onConnected() {
    console.debug("Connected to backend, clearing command buffer");
    this.isConnected = true;
    this.commandsBuffer.forEach((command) => this.commandSink(command));
    this.commandsBuffer = [];
  }

  private createCommandSink() {
    return connectClient(
      "devices",
      (message) => this.handleMessage(message),
      () => this.onConnected()
    );
  }
}

const eventBus = new EventBus();
export default eventBus;
