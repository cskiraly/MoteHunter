COMPONENT=HunterC
BUILD_EXTRA_DEPS = RssiSerialMsg.java SpecAnalyzer.class
CLEAN_EXTRA = *.class RssiSerialMsg.java
CFLAGS += -DCC2420_NO_ADDRESS_RECOGNITION
CFLAGS += -DTOSH_DATA_LENGTH=110	# TinyOS 2.1.1 snoop works only if this is large enough to hold the message
CFLAGS += -DCC2420_DEF_CHANNEL=16

CFLAGS += -I..

RssiSerialMsg.java: RssiToSerial.h
	mig java -target=$(PLATFORM) $(CFLAGS) -java-classname=RssiSerialMsg RssiToSerial.h rssi_serial_msg -o $@

SpecAnalyzer.class: $(wildcard *.java) SpecAnalyzer.java
	javac *.java

include $(MAKERULES)

