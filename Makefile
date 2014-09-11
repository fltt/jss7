# To enabled incremental compilation you need the "jdeps" utility (available in
# OpenJDK 1.8). If not found it will revert to full compilation every time one
# or more source files are modified/added (and ONLY if modified/added).
#
# NOTE: Only file modification/addition is supported, that is, if a (re)source
#       file is removed you have to perform a full compilation ("make clean"
#       followed by "make") in order to get rid of old classes/resources.
#
# If you don't want to generate 1.8 bytecode, define the JAVAC variable in the
# "localdefs.mk" file:
#
#   JAVAC := javac -source 1.7 -target 1.7 -bootclasspath /usr/lib/jvm/java-7-openjdk/jre/lib/rt.jar
#
# Choose source, target and bootclasspath's values appropriate for you project.
#
# NOTE: For small projects a full compilation may be faster than an incremental
#       compilation plus dependencies extration. In such cases you may disable
#       "jdeps" adding "HAVE_JDEPS := false" to "localdefs.mk".

-include localdefs.mk

# To change the value of the following variables, add them to "localdefs.mk".

# Hardware driver support
# NOTE: If DAHDI/Dialogic are not installed system-wide you may also need
#       to define CPPFLAGS and LDFLAGS to point to the headers and libraries
ENABLE_DAHDI ?= false
ENABLE_DIALOGIC ?= false

# Java tools
JAR ?= jar
JAVAC ?= javac
JAVAH ?= javah
JDEPS ?= jdeps

# Every UNIX-like OS should have these (and a Bourne-like shell).
AWK ?= awk
CAT ?= cat
CHMOD ?= chmod
CP ?= cp -f
FGREP ?= fgrep
FIND ?= find
# ??? GIT ?= git
LINK ?= ln -f
MKDIR_P ?= mkdir -p
MV ?= mv
OS ?= uname -o
RM ?= rm -f
SED ?= sed
SHA1 ?= sha1sum
TAR ?= tar
WHICH ?= which
XARGS ?= xargs


# Project structure

resources.jars := jars
resources.libs := libs

SOURCES_PATH := src/main/java
RESOURCES_PATH := src/main/resources
NATIVE_SOURCES_PATH := src/main/native

EXTERNAL_LIBRARIES_DIR := libs

BUILD_DIR := build

SOURCE_FILES_FULL_LIST := $(BUILD_DIR)/source-files
JAVA_DEPENDENCIES := $(BUILD_DIR)/java-dependencies
JAVAC_SOURCEPATHS_LIST := $(BUILD_DIR)/javac-sourcepaths
JAVAC_SOURCE_FILES_LIST := $(BUILD_DIR)/javac-source-files

EXPORTED_CLASSES_LIST := $(BUILD_DIR)/exported-classes
JAVAH_CLASSES_LIST := $(BUILD_DIR)/javah-classes

CLASSES_DIR := $(BUILD_DIR)/classes
RESOURCES_DIR := $(BUILD_DIR)/resources
RESOURCES_FILTER_SCRIPT := $(BUILD_DIR)/sed-script
JARS_DIR := $(BUILD_DIR)/$(resources.jars)

OBJECTS_DIR := $(BUILD_DIR)/obj
INCLUDE_DIR := $(BUILD_DIR)/include
NATIVE_DIR := $(BUILD_DIR)/$(resources.libs)

# stress-tool-[0-9]*.jar is not released
STAGE_DIR := $(BUILD_DIR)/packages
PACKAGE_DIR := packages


release.version := 2.1.0.FINAL-p1

ifeq ($(shell $(OS)),GNU/Linux)
ARCHITECTURE := linux
else
ARCHITECTURE := unknown
endif

LIBRARIES := $(shell test -d $(EXTERNAL_LIBRARIES_DIR) && $(FIND) $(EXTERNAL_LIBRARIES_DIR) -name '*.jar')


compile:


ifndef BUILD_PHASE


ifdef HAVE_JDEPS

ifneq ($(HAVE_JDEPS),true)
JDEPS :=
endif

else # def HAVE_JDEPS

JDEPS := $(firstword $(wildcard $(if $(filter /%,$(JDEPS)),$(JDEPS),$(addsuffix /$(JDEPS),$(subst :, ,$(PATH))))))

ifndef JDEPS
$(info NOTE: jdeps not found: incremental compilation disabled)
endif

endif # def HAVE_JDEPS


CLASSPATH := -cp $(subst $(empty) $(empty),:,$(CLASSES_DIR) $(LIBRARIES))

.PHONY: clean

%:
	@$(MAKE) BUILD_PHASE=1 $@
	@$(MKDIR_P) $(CLASSES_DIR)
ifdef JDEPS
	@if test -s $(JAVAC_SOURCE_FILES_LIST); then \
	  echo "$(JAVAC) -Xlint:deprecation,unchecked -d $(CLASSES_DIR) $(CLASSPATH) @$(JAVAC_SOURCEPATHS_LIST) @$(JAVAC_SOURCE_FILES_LIST)" && \
	  $(JAVAC) -Xlint:deprecation,unchecked -d $(CLASSES_DIR) $(CLASSPATH) @$(JAVAC_SOURCEPATHS_LIST) @$(JAVAC_SOURCE_FILES_LIST) && \
	  if test -f $(JAVA_DEPENDENCIES); then \
	    echo "Updating $(JAVA_DEPENDENCIES)" && \
	    $(FIND) $(CLASSES_DIR) -name '*.class' -cnewer $(JAVA_DEPENDENCIES) >$(JAVA_DEPENDENCIES).tmp && \
	    $(CAT) $(JAVA_DEPENDENCIES).tmp | while read cf; do \
	      cf=$$(echo $$cf | $(SED) -e 's,\$$,\\$$,g'); \
	      $(SED) -Ee "\,^$$cf: ,d" $(JAVA_DEPENDENCIES) >$(JAVA_DEPENDENCIES).bak && \
	      $(MV) $(JAVA_DEPENDENCIES).bak $(JAVA_DEPENDENCIES); \
	    done \
	  else \
	    echo "Building $(JAVA_DEPENDENCIES)" && \
	    $(FIND) $(CLASSES_DIR) -name '*.class' >$(JAVA_DEPENDENCIES).tmp; \
	  fi && \
	  echo "BEGIN {" >$(SOURCE_FILES_FULL_LIST).awk && \
	  $(SED) -E 's,^.*(/$(SOURCES_PATH)/.*)$$,sf["\1"]="&",' $(SOURCE_FILES_FULL_LIST) >>$(SOURCE_FILES_FULL_LIST).awk && \
	  echo "}" >>$(SOURCE_FILES_FULL_LIST).awk && \
	  echo '{ if (sf[$$2]) print $$1 " " sf[$$2] }' >>$(SOURCE_FILES_FULL_LIST).awk && \
	  echo "$(CAT) $(JAVA_DEPENDENCIES).tmp | $(XARGS) $(JDEPS) -v" && \
	  $(CAT) $(JAVA_DEPENDENCIES).tmp | $(XARGS) $(JDEPS) -v | \
	    $(SED) -Ene 's,\.,/,g;s,^ +([^ ]+) *-> *([^ ]+).*$$,$(CLASSES_DIR)/\1.class: /$(SOURCES_PATH)/\2.java,p' | \
	    $(AWK) -f $(SOURCE_FILES_FULL_LIST).awk >>$(JAVA_DEPENDENCIES) && \
	  $(RM) $(SOURCE_FILES_FULL_LIST).awk $(JAVA_DEPENDENCIES).tmp; \
	fi
else # def JDEPS
	@if test -s $(SOURCE_FILES_FULL_LIST) -a  -s $(JAVAC_SOURCE_FILES_LIST); then \
	  echo "$(JAVAC) -Xlint:deprecation,unchecked -d $(CLASSES_DIR) $(CLASSPATH) @$(JAVAC_SOURCEPATHS_LIST) @$(SOURCE_FILES_FULL_LIST)" && \
	  $(JAVAC) -Xlint:deprecation,unchecked -d $(CLASSES_DIR) $(CLASSPATH) @$(JAVAC_SOURCEPATHS_LIST) @$(SOURCE_FILES_FULL_LIST); \
	fi
endif # def JDEPS
	@if test -s $(EXPORTED_CLASSES_LIST); then \
	  $(MKDIR_P) $(INCLUDE_DIR) && \
	  $(CAT) $(EXPORTED_CLASSES_LIST) | while read sf cn; do \
	    if $(FGREP) -q "$$sf" $(JAVAC_SOURCE_FILES_LIST); then \
	      echo "$$cn"; \
	    fi \
	  done >$(JAVAH_CLASSES_LIST); \
	  if test -s $(JAVAH_CLASSES_LIST); then \
	    echo "$(CAT) $(JAVAH_CLASSES_LIST) | $(XARGS) $(JAVAH) -d $(INCLUDE_DIR) $(CLASSPATH)" && \
	    $(CAT) $(JAVAH_CLASSES_LIST) | $(XARGS) $(JAVAH) -d $(INCLUDE_DIR) $(CLASSPATH); \
	  fi \
	fi
	@$(MAKE) BUILD_PHASE=2 $@

clean:
	$(RM) -r $(BUILD_DIR) $(PACKAGE_DIR)


else # ndef BUILD_PHASE


.PHONY: compile jars package

$(BUILD_DIR) $(CLASSES_DIR) $(JARS_DIR) $(NATIVE_DIR) $(PACKAGE_DIR):
	$(MKDIR_P) $@

EXTRA_VARIABLES := resources.jars release.version
JARS_LIST :=

FIND_SOURCES = $(shell test -d $(1)$(SOURCES_PATH) && $(FIND) $(1)$(SOURCES_PATH) -name '*.java')
SOURCES_TO_CLASSES = $(patsubst $(1)$(SOURCES_PATH)/%.java,$(CLASSES_DIR)/%.class,$(2))

CLASSNAME_TO_SOURCEFILE = $(patsubst %,/$(SOURCES_PATH)/%.java,$(subst .,/,$(1)))

FIND_NATIVE_SOURCES = $(shell test -d $(1)$(NATIVE_SOURCES_PATH) && $(FIND) $(1)$(NATIVE_SOURCES_PATH) -name '*.c')

LIBS_AND_VERS := $(shell echo $(basename $(notdir $(LIBRARIES))) | $(SED) -Ee 's,([^ ]*)-(([0-9.]+)(-[^ ]*)?),\1:\2,g')


define PARSE_LIB_AND_VER =

MK_NAME := $(word 1,$(subst :, ,$(1)))
MK_VERSION := $(word 2,$(subst :, ,$(1)))
MK_JARNAME := $$(if $$(MK_VERSION),$$(MK_NAME)-$$(MK_VERSION).jar,$$(MK_NAME).jar)

$$(MK_NAME).version := $$(MK_VERSION)
$$(MK_NAME).basename := $$(MK_JARNAME)
$$(MK_NAME).buildname := $$(filter %/$$(MK_JARNAME),$$(LIBRARIES))
$$(MK_NAME).jarname := $$(addprefix $(resources.jars)/,$$(MK_JARNAME))

EXTRA_VARIABLES += $$(MK_NAME).version $$(MK_NAME).jarname

$$($$(MK_NAME).jarname): $$($$(MK_NAME).buildname) | $(JARS_DIR)
	$(LINK) $$< $$@

endef # PARSE_LIB_AND_VER


$(foreach var,$(LIBS_AND_VERS),$(eval $(call PARSE_LIB_AND_VER,$(var))))


ifeq ($(BUILD_PHASE),1)


SOURCE_DIRECTORIES :=
SOURCE_FILES :=

.PHONY: clean_javac_sourcepaths_list $(JAVAC_SOURCEPATHS_LIST)

clean_javac_sourcepaths_list: | $(BUILD_DIR)
	@: >$(JAVAC_SOURCEPATHS_LIST)

$(JAVAC_SOURCEPATHS_LIST): | clean_javac_sourcepaths_list
	@echo "Building $@" \
	$(foreach var,$(SOURCE_DIRECTORIES),$(file >>$@,-sourcepath $(var)$(SOURCES_PATH)))

.PHONY: $(JAVAC_SOURCE_FILES_LIST)

$(JAVAC_SOURCE_FILES_LIST): | $(BUILD_DIR)
	@: >$@

.PHONY: clean_source_files_full_list $(SOURCE_FILES_FULL_LIST)

clean_source_files_full_list: | $(BUILD_DIR)
	@: >$(SOURCE_FILES_FULL_LIST)

$(SOURCE_FILES_FULL_LIST): | clean_source_files_full_list
	@echo "Building $@" \
	$(foreach var,$(SOURCE_FILES),$(file >>$@,$(var)))


define BUILD_MAKE_RULES =

ifeq ($(1),)
$$(error Missing jar basename)
endif

MK_JARNAME := $(if $(2),$(1)-$(2).jar,$(1).jar)

$(1).version := $(2)
$(1).buildname := $(JARS_DIR)/$$(MK_JARNAME)
$(1).basename := $$(MK_JARNAME)
$(1).jarname := $(resources.jars)/$$(MK_JARNAME)

JARS_LIST += $(JARS_DIR)/$$(MK_JARNAME)
SOURCE_DIRECTORIES += $(3)
SOURCE_FILES += $(4)

compile: $$(call SOURCES_TO_CLASSES,$(3),$(4))

$$(foreach var,$(4),$$(eval $$(call SOURCES_TO_CLASSES,$(3),$$(var)): $$(var)))

$(JARS_DIR)/$$(MK_JARNAME): $$(call SOURCES_TO_CLASSES,$(3),$(4)) $$(foreach var,$(6),$$(value $$(var).buildname))

endef # BUILD_MAKE_RULES


JAVAH_CLASSES :=

.PHONY: clean_exported_classes_list $(EXPORTED_CLASSES_LIST)

clean_exported_classes_list: | $(BUILD_DIR)
	@: >$(EXPORTED_CLASSES_LIST)

$(EXPORTED_CLASSES_LIST): | clean_exported_classes_list
	@echo "Building $@" \
	$(foreach var,$(JAVAH_CLASSES),$(file >>$@,$(call CLASSNAME_TO_SOURCEFILE,$(var)) $(var)))

define BUILD_NATIVE_MAKE_RULES =

ifeq ($(1),)
$$(error Missing library basename)
endif

MK_LIBNAME := $(if $(2),$(1)-$(2).so,$(1).so)

$(1).version := $(2)
$(1).buildname := $(NATIVE_DIR)/$$(MK_LIBNAME)
$(1).basename := $$(MK_LIBNAME)
$(1).libname := $(resources.libs)/$$(MK_LIBNAME)

JAVAH_CLASSES += $(7)

endef # BUILD_NATIVE_MAKE_RULES


$(CLASSES_DIR)/%.class: | $(JAVAC_SOURCEPATHS_LIST) $(JAVAC_SOURCE_FILES_LIST) $(SOURCE_FILES_FULL_LIST) $(EXPORTED_CLASSES_LIST)
	echo $< >>$(JAVAC_SOURCE_FILES_LIST)


else # eq ($(BUILD_PHASE),1)


FIND_RESOURCES = $(shell test -d $(1)$(RESOURCES_PATH) && $(FIND) $(1)$(RESOURCES_PATH) -type f)
RESOURCES_TO_JAR = $(patsubst $(1)$(RESOURCES_PATH)/%,$(RESOURCES_DIR)/$(2)/%,$(3))

SOURCES_TO_OBJECTS = $(patsubst $(2)$(NATIVE_SOURCES_PATH)/%.c,$(OBJECTS_DIR)/$(1)/%.o,$(3))
SOURCES_TO_DEPENDENCIES = $(patsubst $(2)$(NATIVE_SOURCES_PATH)/%.c,$(OBJECTS_DIR)/$(1)/%.d,$(3))

MISSING_RESOURCE_JARS :=


define BUILD_EXTRA_JAR_RULES =

ifndef $(1).jarname
MISSING_RESOURCE_JARS += $(1)
endif

endef # BUILD_EXTRA_JAR_RULES


define BUILD_MAKE_RULES =

ifeq ($(1),)
$$(error Missing jar basename)
endif

MK_JARNAME := $(if $(2),$(1)-$(2).jar,$(1).jar)

$(1).version := $(2)
$(1).buildname := $(JARS_DIR)/$$(MK_JARNAME)
$(1).basename := $$(MK_JARNAME)
$(1).jarname := $(resources.jars)/$$(MK_JARNAME)

EXTRA_VARIABLES += $(1).version $(1).jarname
JARS_LIST += $(JARS_DIR)/$$(MK_JARNAME)

$$($(1).jarname): $$($(1).buildname) | $(JARS_DIR)
	$(LINK) $$< $$@

compile: $$(call SOURCES_TO_CLASSES,$(3),$(4))

$$(foreach var,$(5),$$(eval $$(call RESOURCES_TO_JAR,$(3),$(1),$$(var)): $$(var)))

$$(foreach var,$(6),$$(eval $$(call BUILD_EXTRA_JAR_RULES,$$(var))))

$(JARS_DIR)/$$(MK_JARNAME): $$(call SOURCES_TO_CLASSES,$(3),$(4)) $$(call RESOURCES_TO_JAR,$(3),$(1),$(5)) $$(foreach var,$(6),$$(value $$(var).buildname)) | $(JARS_DIR)
	if test -f $$@; then cmd=u; else cmd=c; fi && \
	$(JAR) $$$${cmd}vf $$@ \
	  $$(addprefix -C $(CLASSES_DIR) ,$$(patsubst $(CLASSES_DIR)/%,'%',$$(filter $(CLASSES_DIR)/%,$$?) \
	                                                                   $$(wildcard $$(patsubst %.class,%$$$$*.class,$$(filter $(CLASSES_DIR)/%,$$?))))) \
	  $$(addprefix -C $(RESOURCES_DIR)/$(1) ,$$(patsubst $(RESOURCES_DIR)/$(1)/%,'%',$$(filter $(RESOURCES_DIR)/%,$$?))) \
	  $$(addprefix -C $(BUILD_DIR) ,$$(patsubst $(BUILD_DIR)/%,'%',$$(filter $(JARS_DIR)/%,$$?)))

endef # BUILD_MAKE_RULES


define BUILD_NATIVE_COMPILE_RULES =

ifeq ($(ARCHITECTURE),unknown)
$$(error Unsupported OS: $(shell $(OS)))
endif

include $$(call SOURCES_TO_DEPENDENCIES,$(1),$(2),$(4))

$$(call SOURCES_TO_DEPENDENCIES,$(1),$(2),$(4)): $(4)
	$(MKDIR_P) $$$$(dirname $$@) && \
	$(CPP) $(CPPFLAGS) -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/$(ARCHITECTURE) -I $(INCLUDE_DIR) -MM \
	  -MT '$$(call SOURCES_TO_DEPENDENCIES,$(1),$(2),$(4)) $$(call SOURCES_TO_OBJECTS,$(1),$(2),$(4))' $$< >$$@

$$(call SOURCES_TO_OBJECTS,$(1),$(2),$(4)): $(4)
	$(MKDIR_P) $$$$(dirname $$@) && \
	$(CC) $(CPPFLAGS) $(CFLAGS) -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/$(ARCHITECTURE) -I $(INCLUDE_DIR) $(3) -c -o $$@ $$<

endef # BUILD_NATIVE_COMPILE_RULES


define BUILD_NATIVE_MAKE_RULES =

ifeq ($(1),)
$$(error Missing library basename)
endif

MK_LIBNAME := $(if $(2),$(1)-$(2).so,$(1).so)

$(1).version := $(2)
$(1).buildname := $(NATIVE_DIR)/$$(MK_LIBNAME)
$(1).basename := $$(MK_LIBNAME)
$(1).libname := $(resources.libs)/$$(MK_LIBNAME)

EXTRA_VARIABLES += $(1).version $(1).libname
JARS_LIST += $(NATIVE_DIR)/$$(MK_LIBNAME)

compile: $$(call SOURCES_TO_OBJECTS,$(1),$(3),$(4))

$$(foreach var,$(4),$$(eval $$(call BUILD_NATIVE_COMPILE_RULES,$(1),$(3),$(5),$$(var))))

$(NATIVE_DIR)/$$(MK_LIBNAME): $$(call SOURCES_TO_OBJECTS,$(1),$(3),$(4)) | $(NATIVE_DIR)
	$(CC) $(LDFLAGS) $(6) -o $$@ $$^

endef # BUILD_NATIVE_MAKE_RULES


.PHONY: clean_resources_filter_script $(RESOURCES_FILTER_SCRIPT)

clean_resources_filter_script: | $(BUILD_DIR)
	@: >$(RESOURCES_FILTER_SCRIPT)

$(RESOURCES_FILTER_SCRIPT): | clean_resources_filter_script
	@echo "Building $@" \
	$(foreach var,$(EXTRA_VARIABLES),$(file >>$(RESOURCES_FILTER_SCRIPT),'-es|$${$(var)}|$($(var))|g'))

$(RESOURCES_DIR)/%: | $(RESOURCES_FILTER_SCRIPT)
	$(MKDIR_P) $$(dirname $@) && \
	$(CP) $< $@ && \
	$(CAT) $(RESOURCES_FILTER_SCRIPT) | $(XARGS) $(SED) -i $@


endif # eq ($(BUILD_PHASE),1)


include asn/asn-impl/build.mk
include cap/cap-api/build.mk
include cap/cap-impl/build.mk
include commons/build.mk
include congestion/build.mk
include hardware/cli/build.mk
include hardware/dahdi/java/build.mk
include hardware/dialogic/java/build.mk
include hardware/linkset/build.mk
include inap/inap-api/build.mk
include inap/inap-impl/build.mk
include isup/isup-api/build.mk
include isup/isup-impl/build.mk
include m3ua/api/build.mk
include m3ua/cli/m3ua/build.mk
include m3ua/cli/sctp/build.mk
include m3ua/impl/build.mk
include management/shell-client/build.mk
include management/shell-server-api/build.mk
include management/shell-server-impl/build.mk
# Contains EXTRA_VARIABLES
include management/shell-transport/build.mk
# ??? test: include map/load/build.mk
include map/map-api/build.mk
include map/map-impl/build.mk
include mtp/mtp-api/build.mk
include mtp/mtp-impl/build.mk
include sccp/sccp-api/build.mk
include sccp/sccp-cli/build.mk
include sccp/sccp-impl/build.mk
include sctp/sctp-api/build.mk
include sctp/sctp-impl/build.mk
include scheduler/build.mk
# Contains EXTRA_VARIABLES
include service/build.mk
include sgw/boot/build.mk
include sgw/gateway/build.mk
include statistics/api/build.mk
include statistics/impl/build.mk
include stream/api/build.mk
include tcap-ansi/tcap-ansi-api/build.mk
include tcap-ansi/tcap-ansi-impl/build.mk
include tcap/tcap-api/build.mk
include tcap/tcap-impl/build.mk
include tools/simulator/bootstrap/build.mk
include tools/simulator/core/build.mk
include tools/simulator/gui/build.mk
include tools/trace-parser/parser/build.mk

# Contains EXTRA_VARIABLES
include docs/sources/build.mk
# Documentation contains maven-jdocbook-plugin's specific extensions which
# prevent compilation from generic tools (tried both xsltproc and fop)
# include docs/jdocbook-mobicents/build.mk

ifeq ($(ENABLE_DAHDI),true)
include hardware/dahdi/native/build.mk
endif

ifeq ($(ENABLE_DIALOGIC),true)
include hardware/dialogic/native/build.mk
endif

-include $(JAVA_DEPENDENCIES)

ifdef MISSING_RESOURCE_JARS
$(error Missing extra jar(s): $(MISSING_RESOURCE_JARS))
endif

jars: $(JARS_LIST)


MISC_BUILD_LIST := release/README.TXT


ASN_JAR_BUILD_LIST := $(asn.buildname)


SCTP_JAR_BUILD_LIST := $(sctp-api.buildname) $(sctp-impl.buildname)


SS7_SERVICE_JAR_BUILD_LIST := $(asn.buildname) $(cap-api.buildname) $(capi-impl.buildname) \
                              $(commons.buildname) $(congestion.buildname) $(inap-api.buildname) \
                              $(inap-impl.buildname) $(isup-api.buildname) $(isup-impl.buildname) \
                              $(javolution.buildname) $(linkset.buildname) $(m3ua-api.buildname) \
                              $(m3ua-impl.buildname) $(map-api.buildname) $(map-impl.buildname) \
                              $(mobicents-dialogic.buildname) $(mobicents-ss7.buildname) \
                              $(mtp.buildname) $(mtp-api.buildname) $(sccp-api.buildname) \
                              $(sccp-impl.buildname) $(scheduler.buildname) $(sctp-api.buildname) \
                              $(sctp-impl.buildname) $(shell-server-api.buildname) \
                              $(shell-server-impl.buildname) $(shell-transport.buildname) \
                              $(statistics-api.buildname) $(statistics-impl.buildname) \
                              $(stream.buildname) $(tcap-api.buildname) $(tcap-impl.buildname)

# jboss.server.data.dir, jboss.bind.address
SS7_SERVICE_RESOURCES_LIST := service/src/main/config/jboss-beans.xml \
                              service/src/main/config/jboss-structure.xml


SGW_BIN_BUILD_LIST := sgw/boot/src/main/config/init_redhat.sh \
                      sgw/boot/src/main/config/run.bat \
                      sgw/boot/src/main/config/run.sh

# sgw.home.dir
SGW_CONF_BUILD_LIST := sgw/boot/src/main/config/bootstrap-beans.xml \
                       sgw/boot/src/main/config/log4j.xml

# sgw.home.dir, sgw.bind.address
SGW_DEPLOY_BUILD_LIST := sgw/boot/src/main/config/sgw-beans.xml

# ?? $(activation.buildname) $(dtdparser121.buildname)
# ?? $(jaxb-api.buildname) $(xml-apis.buildname) $(jbossxb.buildname)
SGW_JAR_BUILD_LIST := $(boot.buildname) $(commons.buildname) $(gateway.buildname) \
                      $(java-getopt.buildname) $(javolution.buildname) \
                      $(jboss-common-core.buildname) $(jboss-dependency.buildname) \
                      $(jboss-kernel.buildname) $(jboss-logging-spi.buildname) \
                      $(jboss-mdr.buildname) $(jboss-reflect.buildname) $(jboss-xml-binding.buildname) \
                      $(linkset.buildname) $(log4j.buildname) $(m3ua-api.buildname) \
                      $(m3ua-impl.buildname) $(mobicents-dahdi.buildname) \
                      $(mobicents-dialogic.buildname) $(mtp.buildname) $(mtp-api.buildname) \
                      $(scheduler.buildname) $(sctp-api.buildname) $(sctp-impl.buildname) \
                      $(shell-server-api.buildname) $(shell-server-impl.buildname) \
                      $(shell-transport.buildname) $(stream.buildname)

SGW_NATIVE_BUILD_LIST :=

ifeq ($(ENABLE_DAHDI),true)
SGW_NATIVE_BUILD_LIST += $(libmobicents-dahdi-linux.buildname)
endif

ifeq ($(ENABLE_DIALOGIC),true)
SGW_NATIVE_BUILD_LIST += $(libmobicents-dialogic-linux.buildname)
endif

SGW_DATA_BUILD_LIST := sgw/boot/src/main/config/linksetmanager.xml


SS7_SIMULATOR_BIN_BUILD_LIST := tools/simulator/bootstrap/src/main/config/run.bat \
                                tools/simulator/bootstrap/src/main/config/run.sh

SS7_SIMULATOR_CONF_BUILD_LIST := tools/simulator/bootstrap/src/main/config/log4j.xml

SS7_SIMULATOR_DATA_BUILD_LIST := tools/simulator/bootstrap/src/main/config/data.txt

SS7_SIMULATOR_JAR_BUILD_LIST := $(asn.buildname) $(cap-api.buildname) $(cap-impl.buildname) \
                                $(commons.buildname) $(congestion.buildname) $(java-getopt.buildname) \
                                $(inap-api.buildname) $(inap-impl.buildname) $(isup-api.buildname) \
                                $(isup-impl.buildname) $(javolution.buildname) $(jmxtools.buildname) \
                                $(log4j.buildname) $(m3ua-api.buildname) $(m3ua-impl.buildname) \
                                $(map-api.buildname) $(map-impl.buildname) $(mobicents-dialogic.buildname) \
                                $(mtp.buildname) $(mtp-api.buildname) $(sccp-api.buildname) \
                                $(sccp-impl.buildname) $(sctp-api.buildname) $(sctp-impl.buildname) \
                                $(simulator-core.buildname) $(simulator-gui.buildname) \
                                $(statistics-api.buildname) $(statistics-impl.buildname) \
                                $(stream.buildname) $(tcap-api.buildname) $(tcap-impl.buildname)


SS7_PROTOCOLS_BUILD_LIST := $(cap-api.buildname) $(cap-impl.buildname) $(inap-api.buildname) \
                            $(inap-impl.buildname) $(isup-api.buildname) $(isup-impl.buildname) \
                            $(map-api.buildname) $(map-impl.buildname) $(tcap-api.buildname) \
                            $(tcap-impl.buildname)


SS7_SHELL_BIN_BUILD_LIST := management/shell-client/src/main/config/ss7-cli.bat \
                            management/shell-client/src/main/config/ss7-cli.sh

# ?? $(jansi.buildname)
SS7_SHELL_LIB_BUILD_LIST := $(javolution.buildname) $(jreadline.buildname) $(linkset-cli.buildname) \
                            $(m3ua-cli-m3ua.buildname) $(m3ua-cli-sctp.buildname) \
                            $(mobicents-ss7-shell.buildname) $(sccp-cli.buildname) \
                            $(shell-transport.buildname)


SS7_NATIVE_LIB_BUILD_LIST :=

ifeq ($(ENABLE_DAHDI),true)
SS7_NATIVE_LIB_BUILD_LIST += $(libmobicents-dahdi-linux.buildname)
endif

ifeq ($(ENABLE_DIALOGIC),true)
SS7_NATIVE_LIB_BUILD_LIST += $(libmobicents-dialogic-linux.buildname)
endif


# ???
# SS7_DOC_BUILD_LIST := ?


ifeq ($(BUILD_PHASE),1)


$(PACKAGE_DIR)/mobicents-ss7-$(release.version).tar.gz: $(ASN_JAR_BUILD_LIST) $(SCTP_JAR_BUILD_LIST) $(SS7_SERVICE_JAR_BUILD_LIST) $(boot.buildname) $(SGW_JAR_BUILD_LIST) $(simulator-bootstrap.buildname) $(SS7_SIMULATOR_JAR_BUILD_LIST) $(SS7_PROTOCOLS_BUILD_LIST) $(SS7_SHELL_LIB_BUILD_LIST)

package: $(PACKAGE_DIR)/mobicents-ss7-$(release.version).tar.gz


else #eq ($(BUILD_PHASE),1)


INTO_STAGE = $(addprefix $(STAGE_DIR)/$(if $(1),$(1)/,),$(notdir $(2)))


define BUILD_PACKAGE_RULES =

$(2) := $$(call INTO_STAGE,$(3),$$($(1)))

$(STAGE_DIR)/$(3):
	$(MKDIR_P) $$@

$$(foreach var,$$($(1)),$$(eval $$(call INTO_STAGE,$(3),$$(var)): $$(var) | $(STAGE_DIR)/$(3)))

endef # BUILD_PACKAGE_RULES


define ADD_PACKAGE_RULES =

$(2) += $$(call INTO_STAGE,$(3),$$($(1)))

$(STAGE_DIR)/$(3):
	$(MKDIR_P) $$@

$$(foreach var,$$($(1)),$$(eval $$(call INTO_STAGE,$(3),$$(var)): $$(var) | $(STAGE_DIR)/$(3)))

endef # ADD_PACKAGE_RULES


$(STAGE_DIR)/%:
	$(LINK) $^ $@

$(STAGE_DIR)/%.sh:
	$(CP) $^ $@ && $(CHMOD) a+x $@


$(eval $(call BUILD_PACKAGE_RULES,MISC_BUILD_LIST,MISC_STAGE_LIST,))

$(eval $(call BUILD_PACKAGE_RULES,ASN_JAR_BUILD_LIST,ASN_STAGE_LIST,asn))

$(eval $(call BUILD_PACKAGE_RULES,SCTP_JAR_BUILD_LIST,SCTP_STAGE_LIST,sctp))

$(eval $(call BUILD_PACKAGE_RULES,SS7_SERVICE_JAR_BUILD_LIST,SS7_SERVICE_STAGE_LIST,ss7/mobicents-ss7-service/lib))
$(eval $(call ADD_PACKAGE_RULES,SS7_SERVICE_RESOURCES_LIST,SS7_SERVICE_STAGE_LIST,ss7/mobicents-ss7-service/META-INF))

$(eval $(call BUILD_PACKAGE_RULES,SGW_BIN_BUILD_LIST,SGW_STAGE_LIST,ss7/mobicents-sgw/bin))
$(eval $(call ADD_PACKAGE_RULES,SGW_CONF_BUILD_LIST,SGW_STAGE_LIST,ss7/mobicents-sgw/conf))
$(eval $(call ADD_PACKAGE_RULES,SGW_DEPLOY_BUILD_LIST,SGW_STAGE_LIST,ss7/mobicents-sgw/deploy))
$(eval $(call ADD_PACKAGE_RULES,SGW_JAR_BUILD_LIST,SGW_STAGE_LIST,ss7/mobicents-sgw/lib))
$(eval $(call ADD_PACKAGE_RULES,SGW_NATIVE_BUILD_LIST,SGW_STAGE_LIST,ss7/mobicents-sgw/native))
$(eval $(call ADD_PACKAGE_RULES,SGW_DATA_BUILD_LIST,SGW_STAGE_LIST,ss7/mobicents-sgw/ss7))

SGW_STAGE_LIST += $(STAGE_DIR)/ss7/mobicents-sgw/bin/run.jar

$(STAGE_DIR)/ss7/mobicents-sgw/bin/run.jar: $(boot.buildname) | $(STAGE_DIR)/ss7/mobicents-sgw/bin

$(eval $(call BUILD_PACKAGE_RULES,SS7_SIMULATOR_BIN_BUILD_LIST,SS7_SIMULATOR_STAGE_LIST,ss7/mobicents-ss7-simulator/bin))
$(eval $(call ADD_PACKAGE_RULES,SS7_SIMULATOR_CONF_BUILD_LIST,SS7_SIMULATOR_STAGE_LIST,ss7/mobicents-ss7-simulator/conf))
$(eval $(call ADD_PACKAGE_RULES,SS7_SIMULATOR_DATA_BUILD_LIST,SS7_SIMULATOR_STAGE_LIST,ss7/mobicents-ss7-simulator/data))
$(eval $(call ADD_PACKAGE_RULES,SS7_SIMULATOR_JAR_BUILD_LIST,SS7_SIMULATOR_STAGE_LIST,ss7/mobicents-ss7-simulator/lib))

SS7_SIMULATOR_STAGE_LIST += $(STAGE_DIR)/ss7/mobicents-ss7-simulator/bin/run.jar

$(STAGE_DIR)/ss7/mobicents-ss7-simulator/bin/run.jar: $(simulator-bootstrap.buildname) | $(STAGE_DIR)/ss7/mobicents-ss7-simulator/bin

$(eval $(call BUILD_PACKAGE_RULES,SS7_PROTOCOLS_BUILD_LIST,SS7_PROTOCOLS_STAGE_LIST,ss7/protocols))

$(eval $(call BUILD_PACKAGE_RULES,SS7_SHELL_BIN_BUILD_LIST,SS7_SHELL_STAGE_LIST,ss7/shell/bin))
$(eval $(call ADD_PACKAGE_RULES,SS7_SHELL_LIB_BUILD_LIST,SS7_SHELL_STAGE_LIST,ss7/shell/lib))

$(eval $(call BUILD_PACKAGE_RULES,SS7_NATIVE_LIB_BUILD_LIST,SS7_NATIVE_STAGE_LIST,ss7/native))

$(STAGE_DIR)/ss7:
	$(MKDIR_P) $@

SS7_MISC_STAGE_LIST := $(STAGE_DIR)/ss7/build.xml

$(STAGE_DIR)/ss7/build.xml: release/release-build.xml | $(STAGE_DIR)/ss7


# ???
# $(PACKAGE_DIR)/mobicents-ss7-$(release.version)-src.tar.gz: | $(PACKAGE_DIR)
# 	$(GIT) archive --format=tar.gz --prefix=ss7/ -o $@ HEAD

$(PACKAGE_DIR)/mobicents-ss7-$(release.version).tar.gz: $(MISC_STAGE_LIST) $(ASN_STAGE_LIST) $(SCTP_STAGE_LIST) $(SS7_SERVICE_STAGE_LIST) $(SGW_STAGE_LIST) $(SS7_SIMULATOR_STAGE_LIST) $(SS7_PROTOCOLS_STAGE_LIST) $(SS7_SHELL_STAGE_LIST) $(SS7_NATIVE_STAGE_LIST) $(SS7_MISC_STAGE_LIST) | $(PACKAGE_DIR)
	$(RM) $@ && $(TAR) -czv -f $@ -C $(STAGE_DIR) $(patsubst $(STAGE_DIR)/%,'%',$^)

$(PACKAGE_DIR)/%.sha1.asc: $(PACKAGE_DIR)/%
	$(SHA1) $< | (read a b; echo $$a) >$@

package: $(PACKAGE_DIR)/mobicents-ss7-$(release.version).tar.gz \
         $(PACKAGE_DIR)/mobicents-ss7-$(release.version).tar.gz.sha1.asc


endif # eq ($(BUILD_PHASE),1)


endif # ndef BUILD_PHASE
