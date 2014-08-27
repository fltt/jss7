MK_NAME := inap-impl
MK_VERSION := 2.1.0.FINAL

MK_DIR := $(dir $(lastword $(MAKEFILE_LIST)))

MK_SOURCES := $(call FIND_SOURCES,$(MK_DIR))

$(eval $(call BUILD_MAKE_RULES,$(MK_NAME),$(MK_VERSION),$(MK_DIR),$(MK_SOURCES),,))
