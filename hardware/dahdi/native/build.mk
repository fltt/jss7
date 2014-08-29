MK_NAME := libmobicents-dahdi-linux
# -p1
MK_VERSION :=

MK_DIR := $(dir $(lastword $(MAKEFILE_LIST)))

MK_SOURCES := $(call FIND_NATIVE_SOURCES,$(MK_DIR))
MK_CFLAGS := -fPIC -O
MK_LDFLAGS := -shared -lc -ldl
MK_JAVAH_CLASSES := org.mobicents.ss7.hardware.dahdi.Channel \
                    org.mobicents.ss7.hardware.dahdi.Selector

$(eval $(call BUILD_NATIVE_MAKE_RULES,$(MK_NAME),$(MK_VERSION),$(MK_DIR),$(MK_SOURCES),$(MK_CFLAGS),$(MK_LDFLAGS),$(MK_JAVAH_CLASSES)))
