MK_NAME := shell-transport
MK_VERSION := 2.1.0.FINAL

MK_DIR := $(dir $(lastword $(MAKEFILE_LIST)))

MK_SOURCES := $(call FIND_SOURCES,$(MK_DIR))
MK_RESOURCES := $(call FIND_RESOURCES,$(MK_DIR))

cli.name := Mobicents CLI
cli.vendor := TeleStax
cli.version := $(MK_VERSION)
cli.prefix := mobicents

# cli.name := TelScale CLI
# cli.vendor := TeleStax
# cli.version := $(MK_VERSION)
# cli.prefix := telscale

EXTRA_VARIABLES += cli.name cli.vendor cli.version cli.prefix

$(eval $(call BUILD_MAKE_RULES,$(MK_NAME),$(MK_VERSION),$(MK_DIR),$(MK_SOURCES),$(MK_RESOURCES),))
