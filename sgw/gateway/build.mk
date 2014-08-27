MK_NAME := gateway
MK_VERSION := 2.1.0.FINAL

MK_DIR := $(dir $(lastword $(MAKEFILE_LIST)))

MK_SOURCES := $(call FIND_SOURCES,$(MK_DIR))
MK_RESOURCES := $(call FIND_RESOURCES,$(MK_DIR))

sgw.name := Mobicents SGW
sgw.vendor := TeleStax
sgw.version := $(MK_VERSION)

EXTRA_VARIABLES += sgw.name sgw.vendor sgw.version

$(eval $(call BUILD_MAKE_RULES,$(MK_NAME),$(MK_VERSION),$(MK_DIR),$(MK_SOURCES),$(MK_RESOURCES),))
