GEN=RssiMsg.java RssiRxMsg.java RssiAckMsg.java
GEN+=SetPingerMsg.java
GEN+=SetChannelMsg.java

all: motehunter.jar RssiMsg.java RssiRxMsg.java RssiAckMsg.java

RssiMsg.java: ../Rssi.h
	mig -target=null -java-classname=RssiMsg java ../Rssi.h rssi -o $@

RssiRxMsg.java: ../Rssi.h
	mig -target=null -java-classname=RssiRxMsg java ../Rssi.h rssi_rx -o $@

RssiAckMsg.java: ../Rssi.h
	mig -target=null -java-classname=RssiAckMsg java ../Rssi.h rssi_ack -o $@

SetPingerMsg.java: ../Commands.h
	mig -target=null -java-classname=SetPingerMsg java ../Commands.h set_pinger -o $@

SetChannelMsg.java: ../Commands.h
	mig -target=null -java-classname=SetChannelMsg java ../Commands.h set_channel -o $@

motehunter.jar: MoteHunter.class
	jar cf $@ *.class

MoteHunter.class: $(wildcard *.java) $(GEN)
	javac *.java

clean:
	rm -f *.class $(GEN)

veryclean: clean
	rm motehunter.jar
