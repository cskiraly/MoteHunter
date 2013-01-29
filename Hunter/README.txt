README for MoteHunter/Hunter
Author/Contact: csaba.kiraly@gmail.com --- http://csabakiraly.com

Description:

Hunter is the TinyOS component of the MoteHunter tool. It has to be
compiled for the TelosB platform and loaded on a suitable TelosB
compatible mote.

The mote works both as a standalone MoteHunter tool, or, if connected to
a PC and paired with the MoteHunter Java application, as a full fledged
MoteHunter application. For more details, please see the paper

"Where's the Mote? Ask the MoteHunter!", Csaba Kiraly, Gian Pietro Picco
In Proceedings of the 7th IEEE International Workshop on Practical
Issues in Building Sensor Network Applications (SenseApp), October 2012
http://csabakiraly.com/files/preprints/kiraly-2012SenseApp-MoteHunter.pdf

Compilation:

A working TinyOS build envoronment is required to compile the code.
Please verify that the TOSDIR environment variable is set. Compile the
code with:

  make telosb

install it on a mote (assuming it is attached to USB0):

  make telosb install bsl,/dev/ttyUSB0

Usage:

License:

MoteHunter is free software: you can redistribute it and/or modify it
under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at
your option) any later version.

MoteHunter is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero
General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with MoteHunter.  If not, see <http://www.gnu.org/licenses/>.

MoteHunter is based on many other open source projects licensed under
BSD and/or INTEL licenses. Code of the following TinyOS projects had been
reused:

 - Oscilloscope: David Gay
 - BaseStation15.4:  Phil Buonadonna, Gilman Tolle, David Gay
 - RssiToSerial: Jared Hill
 - TestAcks: David Moss

Known bugs/limitations:

Tested and works with TinyOS 2.1.1 and various TelosB compatible motes.
Minor modifications might be needed for other TinyOS version.
