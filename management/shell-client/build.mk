MK_NAME := mobicents-ss7-shell
# -p1
MK_VERSION :=

MK_DIR := $(dir $(lastword $(MAKEFILE_LIST)))

MK_SOURCES := $(call FIND_SOURCES,$(MK_DIR))
MK_RESOURCES := $(call FIND_RESOURCES,$(MK_DIR))

$(eval $(call BUILD_MAKE_RULES,$(MK_NAME),$(MK_VERSION),$(MK_DIR),$(MK_SOURCES),$(MK_RESOURCES),))
