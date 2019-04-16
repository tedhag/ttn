#!/bin/bash

#Very rough shutdown...this is to brutal for this application, but this will do for this small test
PID=`pgrep -f progatec-ttn.jar`
kill -9 ${PID}


