MK_NAME := simulator-bootstrap
MK_VERSION := 2.1.0.FINAL-p1

MK_DIR := $(dir $(lastword $(MAKEFILE_LIST)))

MK_SOURCES := $(call FIND_SOURCES,$(MK_DIR))
MK_RESOURCES := $(call FIND_RESOURCES,$(MK_DIR))

$(eval $(call BUILD_MAKE_RULES,$(MK_NAME),$(MK_VERSION),$(MK_DIR),$(MK_SOURCES),$(MK_RESOURCES),))
