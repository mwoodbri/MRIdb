#!/usr/bin/env python

import sys

sys.path.append('/opt/ISD_dicom_tool')

from ISDDTlib import dcm_lib

sys.path.append('/opt/pynifti-0.20100607.1')

dcm_lib.dicom_2_nifti(sys.argv[1], sys.argv[2])