MK_NAME := mobicents-ss7
MK_VERSION := 2.1.0.FINAL

MK_DIR := $(dir $(lastword $(MAKEFILE_LIST)))

MK_SOURCES := $(call FIND_SOURCES,$(MK_DIR))
MK_RESOURCES := $(call FIND_RESOURCES,$(MK_DIR))

ss7.name := Mobicents jSS7
ss7.vendor := TeleStax
ss7.version := $(MK_VERSION)

EXTRA_VARIABLES += ss7.name ss7.vendor ss7.version

$(eval $(call BUILD_MAKE_RULES,$(MK_NAME),$(MK_VERSION),$(MK_DIR),$(MK_SOURCES),$(MK_RESOURCES),))
