# MK_NAME := mobicents-ss7-docs-sources
MK_NAME := mobicents-ss7-docs-sources-mobicents
MK_VERSION := 2.1.0.FINAL

MK_DIR := $(dir $(lastword $(MAKEFILE_LIST)))

MK_RESOURCES := $(call FIND_RESOURCES,$(MK_DIR))

THIS.PLATFORM := Mobicents
JEE.PLATFORM := JBoss Application Server
THIS.ISSUE_TRACKER_URL := http://code.google.com/p/jss7/issues/list
THIS.RELEASE_SOURCE_CODE_URL := http://code.google.com/p/jss7/source/browse/
THIS.TRUNK_SOURCE_CODE_URL := http://code.google.com/p/jss7
THIS.RELEASE_BINARY_URL := http://code.google.com/p/jss7/wiki/Downloads?tm=2
author.email.amit := amit.bhayani (at) gmail.com
author.email.bartosz := baranowb (at) gmail.com
author.email.sergey := serg.vetyutnev (at) gmail.com
author.email.vinu := vinu.saseedaran (at) gmail.com
docs.application.name := jSS7 Stack
docs.application.dirname := ss7
docs.bookid := SS7Stack
version := $(MK_VERSION)

EXTRA_VARIABLES += THIS.PLATFORM JEE.PLATFORM THIS.ISSUE_TRACKER_URL THIS.RELEASE_SOURCE_CODE_URL \
                   THIS.TRUNK_SOURCE_CODE_URL THIS.RELEASE_BINARY_URL author.email.amit \
                   author.email.bartosz author.email.sergey author.email.vinu docs.application.name \
                   docs.application.dirname docs.bookid version

$(eval $(call BUILD_MAKE_RULES,$(MK_NAME),$(MK_VERSION),$(MK_DIR),,$(MK_RESOURCES),))
