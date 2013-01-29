README for MoteHunter/Prey
Author/Contact: csaba.kiraly@gmail.com --- http://csabakiraly.com

Description:

Prey is a small TinyOS module that can be integrated in mote code.
It improves the MoteHunter tool's capabilities while searching for
a lost mote.

Usage:

 1) add MoteHunter/Prey to your main application as follows:

  implementation {
    ...
    components PreyC;
    ...
  }

 2) add MoteHunter/Prey to the include path in your Makefile as
 follows:

  MOTEHUNTERDIR := $(TOSROOT)/apps/MoteHunter
  CFLAGS += -I$(MOTEHUNTERDIR)/Prey/ -I$(MOTEHUNTERDIR)


Known bugs/limitations:

None.
