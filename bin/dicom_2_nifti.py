#!/usr/bin/env python

import sys
from ISDDTlib import dcm_lib

dcm_lib.dicom_2_nifti(sys.argv[1], sys.argv[2])