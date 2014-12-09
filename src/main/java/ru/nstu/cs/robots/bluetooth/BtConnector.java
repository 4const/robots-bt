package ru.nstu.cs.robots.bluetooth;

public interface BtConnector {

	void sendMessage();

	int readState();
}
