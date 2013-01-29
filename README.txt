README for MoteHunter
Author/Contact: csabakiraly@gmail.com --- http://csabakiraly.com

Description:

Contrary to laboratory environments, real-world wireless sensor network
deployments face harsh conditions where motes can be lost during
deployment or in operation. MoteHunter is a tool designed to support
searching for such lost motes. It uses a directional antenna, a digital
compass, and RSSI measurement, and provides a Java GUI to assist field
work. It can be used to search for any mote compliant with IEEE
802.15.4, although a special small-footprint software component can be
integrated with the mote’s application to improve the search process.

For more details, see http://csabakiraly.com/content/motehunter or the
following paper:

Kiraly, Csaba, and Gian Pietro Picco. "Where's the Mote? Ask the
MoteHunter!" In 7th IEEE International Workshop on Practical Issues in
Building Sensor Network Applications 2012 (SenseApp 2012), 986-994.
Clearwater, Florida, USA: IEEE, 2012.

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

To the best of our knowledge these are GPL compatible licenses, and thus
we release this code under the GNU Affero General Public License. In
case of doubt, please also consult the original licenses.

To facilitate code reuse, the Prey component is also licensed under the
terms of LGPL v3.
