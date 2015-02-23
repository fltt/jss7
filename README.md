Mobicents jSS7
==============
This is a clone of [Mobicents jSS7].
It includes many bug fixes -- mainly in the MTP stack -- and a few new
features.

The new features include:

* the `-r` option of the SS7 Simulator's GUI: allows to specify the RMI
  port used by the core part
* the `-s` option of the SS7 Simulator's Core: allows to start the
  simulation immediately without waiting for an explicity command from
  the user
* a second MAP Stack for the SS7 Simulator: this is required to properly
  simulate both the MSC and the HLR
* the `-c` option of the Shell Client (CLI): allows to specify one or
  more (if specified multiple times) commands to be executed immediately
  upon start-up
* a new build system base on GNU `make`: for instruction on its use read
  the [build instructions][] -- note that the Maven base build system is
  still available and required to build the documentation and run the
  tests
* a second Nature of Address parameter in the simulator's SCCP
  configuration used to build additional routing rules
* add to UserDataImpl helper functions to split long messages

The bug fixes include:

* a persistence bug in Linkset management
* SLS bits allocation in M3UA AS routing policy
* frame queueing/verification in MTP2
* links and linksets activation/deactivation
* badly dimensioned DAHDI buffers
* HDLC broken write policy (it was "write and forget", now it is "write
  and retry again later, if the buffer is full")
* Shell CLI reactivity
* SLS to MTP2 links mapping algorithm
* SLTM/SLTA ping scheduling and TRA sending
* and many others (run `git log` for a detailed list)


[Mobicents jSS7]: https://code.google.com/p/jss7/
[build instructions]: INSTALL.md
