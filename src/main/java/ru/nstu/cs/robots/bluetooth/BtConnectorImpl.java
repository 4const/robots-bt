package ru.nstu.cs.robots.bluetooth;

public class BtConnectorImpl implements BtConnector {

	private int portId;

	public BtConnectorImpl(int portId) {
		this.portId = portId;
	}

	@Override
	public void sendMessage() {

	}

	@Override
	public int readState() {
		return 0;
	}
}
