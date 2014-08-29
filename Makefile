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
CAT ?= cat
CP ?= cp -lf
FGREP ?= fgrep
FIND ?= find
MKDIR_P ?= mkdir -p
MV ?= mv
OS ?= uname -o
RM ?= rm -f
SED ?= sed
WHICH ?= which
XARGS ?= xargs


# Project structure

resources.jars := jars
resources.libs := libs

SOURCES_PATH := src/main/java
RESOURCES_PATH := src/main/resources
NATIVE_SOURCES_PATH := src/main/native


BUILD_DIR := build

SOURCE_FILES_FULL_LIST := $(BUILD_DIR)/source-files
JAVA_DEPENDENCIES := $(BUILD_DIR)/java-dependencies
JAVAC_SOURCEPATHS_LIST := $(BUILD_DIR)/javac-sourcepaths
JAVAC_SOURCE_FILES_LIST := $(BUILD_DIR)/javac-source-files

EXPORTED_CLASSES_LIST := $(BUILD_DIR)/exported-classes
JAVAH_CLASSES_LIST := $(BUILD_DIR)/javah-classes

CLASSES_DIR := $(BUILD_DIR)/classes
RESOURCES_DIR := $(BUILD_DIR)/resources
JARS_DIR := $(BUILD_DIR)/$(resources.jars)

OBJECTS_DIR := $(BUILD_DIR)/obj
INCLUDE_DIR := $(BUILD_DIR)/include
NATIVE_DIR := $(BUILD_DIR)/$(resources.libs)

# stress-tool-[0-9]*.jar is not released
PACKAGE_DIR := packages


ifeq ($(shell $(OS)),GNU/Linux)
ARCHITECTURE := linux
else
ARCHITECTURE := unknown
endif


compile:


ifndef BUILD_PHASE


ifdef HAVE_JDEPS

ifneq ($(HAVE_JDEPS),true)
JDEPS :=
endif

else # def HAVE_JDEPS

JDEPS := $(firstword $(wildcard $(if $(filter /%,$(JDEPS)),$(JDEPS),$(addsuffix /$(JDEPS),$(subst :, ,$(PATH)))) ))

ifndef JDEPS
$(info NOTE: jdeps not found: incremental compilation disabled)
endif

endif # def HAVE_JDEPS


LIBRARIES := $(strip $(wildcard libs/*.jar libs/*/*.jar))
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
	      $(SED) -Ee "\,^$$cf: ,d" $(JAVA_DEPENDENCIES) >$(JAVA_DEPENDENCIES).bak && \
	      $(MV) $(JAVA_DEPENDENCIES).bak $(JAVA_DEPENDENCIES); \
	    done \
	  else \
	    echo "Building $(JAVA_DEPENDENCIES)" && \
	    $(FIND) $(CLASSES_DIR) -name '*.class' >$(JAVA_DEPENDENCIES).tmp; \
	  fi && \
	  echo "$(CAT) $(JAVA_DEPENDENCIES).tmp | $(XARGS) $(JDEPS) -v" && \
	  $(CAT) $(JAVA_DEPENDENCIES).tmp | $(XARGS) $(JDEPS) -v | \
	    $(SED) -Ene 's,\.,/,g;s,^ +([^ ]+) *-> *([^ ]+).*$$,"-es|^([^ ]*/$(SOURCES_PATH)/\2\\.java)$$|$(CLASSES_DIR)/\1\\.class: \\1|p;t",p' | \
	    $(XARGS) -I % $(SED) -En % $(SOURCE_FILES_FULL_LIST) >>$(JAVA_DEPENDENCIES) && \
	  $(RM) $(JAVA_DEPENDENCIES).tmp; \
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

$(BUILD_DIR) $(CLASSES_DIR) $(JARS_DIR) $(NATIVE_DIR):
	$(MKDIR_P) $@

JARS_LIST :=

FIND_SOURCES = $(shell test -d $(1)$(SOURCES_PATH) && $(FIND) $(1)$(SOURCES_PATH) -name '*.java')
SOURCES_TO_CLASSES = $(patsubst $(1)$(SOURCES_PATH)/%.java,$(CLASSES_DIR)/%.class,$(2))

CLASSNAME_TO_SOURCEFILE = $(patsubst %,/$(SOURCES_PATH)/%.java,$(subst .,/,$(1)))

FIND_NATIVE_SOURCES = $(shell test -d $(1)$(NATIVE_SOURCES_PATH) && $(FIND) $(1)$(NATIVE_SOURCES_PATH) -name '*.c')


ifeq ($(BUILD_PHASE),1)


SOURCE_DIRECTORIES :=
SOURCE_FILES :=

.PHONY: clean_javac_sourcepaths_list $(JAVAC_SOURCEPATHS_LIST)

clean_javac_sourcepaths_list: | $(BUILD_DIR)
	@: >$(JAVAC_SOURCEPATHS_LIST)

$(JAVAC_SOURCEPATHS_LIST): | clean_javac_sourcepaths_list
	@echo "Building $@"
	@$(foreach var,$(SOURCE_DIRECTORIES),$(file >>$@,-sourcepath $(var)$(SOURCES_PATH)))

.PHONY: $(JAVAC_SOURCE_FILES_LIST)

$(JAVAC_SOURCE_FILES_LIST): | $(BUILD_DIR)
	@: >$@

.PHONY: clean_source_files_full_list $(SOURCE_FILES_FULL_LIST)

clean_source_files_full_list: | $(BUILD_DIR)
	@: >$(SOURCE_FILES_FULL_LIST)

$(SOURCE_FILES_FULL_LIST): | clean_source_files_full_list
	@echo "Building $@"
	@$(foreach var,$(SOURCE_FILES),$(file >>$@,$(var)))


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

$(JARS_DIR)/$$(MK_JARNAME): $$(call SOURCES_TO_CLASSES,$(3),$(4)) $$(foreach var,$(6),$$(value $$(var).buildname)) $$(call LIB_TO_JAR,$$(call MK_JARS_TO_LIB,$(6)))

endef # BUILD_MAKE_RULES


JAVAH_CLASSES :=

.PHONY: clean_exported_classes_list $(EXPORTED_CLASSES_LIST)

clean_exported_classes_list: | $(BUILD_DIR)
	@: >$(EXPORTED_CLASSES_LIST)

$(EXPORTED_CLASSES_LIST): | clean_exported_classes_list
	@echo "Building $@"
	@$(foreach var,$(JAVAH_CLASSES),$(file >>$@,$(call CLASSNAME_TO_SOURCEFILE,$(var)) $(var)))

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


EXTRA_VARIABLES := resources.jars

FIND_RESOURCES = $(shell test -d $(1)$(RESOURCES_PATH) && $(FIND) $(1)$(RESOURCES_PATH) -type f)
RESOURCES_TO_JAR = $(patsubst $(1)$(RESOURCES_PATH)/%,$(RESOURCES_DIR)/$(2)/%,$(3))
MK_JARS_TO_LIB = $(wildcard $(patsubst %,libs/%.jar,$(1)) $(patsubst %,libs/%-[0-9]*.jar,$(1)) \
                            $(patsubst %,libs/*/%.jar,$(1)) $(patsubst %,libs/*/%-[0-9]*.jar,$(1)))
LIB_TO_JAR = $(addprefix $(JARS_DIR)/,$(notdir $(1)))

SOURCES_TO_OBJECTS = $(patsubst $(2)$(NATIVE_SOURCES_PATH)/%.c,$(OBJECTS_DIR)/$(1)/%.o,$(3))
SOURCES_TO_DEPENDENCIES = $(patsubst $(2)$(NATIVE_SOURCES_PATH)/%.c,$(OBJECTS_DIR)/$(1)/%.d,$(3))

RESOURCES_FILTER_SCRIPT = $(foreach var,$(EXTRA_VARIABLES), -e 's|$${$(var)}|$($(var))|g')

MISSING_RESOURCE_JARS :=


define BUILD_EXTRA_JAR_RULES =

ifndef $(1).jarname

MK_EXTERNAL_JARS := $$(firstword $$(call MK_JARS_TO_LIB,$(1)))

$(1).jarname := $$(addprefix $(resources.jars)/,$$(notdir $$(MK_EXTERNAL_JARS)))

ifdef $(1).jarname

$(1).version := $$(patsubst $(1)-%.jar,%,$$(filter $(1)-%.jar,$$(notdir $$(MK_EXTERNAL_JARS))))
$(1).basename := $$(notdir $$(MK_EXTERNAL_JARS))

EXTRA_VARIABLES += $(1).version $(1).jarname

$$(call LIB_TO_JAR,$$(MK_EXTERNAL_JARS)): $$(MK_EXTERNAL_JARS) | $(JARS_DIR)
	$(CP) $$< $$@

else # def $(1).jarname

MISSING_RESOURCE_JARS += $(1)

endif # def $(1).jarname

endif # ndef $(1).jarname

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

compile: $$(call SOURCES_TO_CLASSES,$(3),$(4))

$$(foreach var,$(5),$$(eval $$(call RESOURCES_TO_JAR,$(3),$(1),$$(var)): $$(var)))

$$(foreach var,$(6),$$(eval $$(call BUILD_EXTRA_JAR_RULES,$$(var))))

$(JARS_DIR)/$$(MK_JARNAME): $$(call SOURCES_TO_CLASSES,$(3),$(4)) $$(call RESOURCES_TO_JAR,$(3),$(1),$(5)) $$(foreach var,$(6),$$(value $$(var).buildname)) $$(call LIB_TO_JAR,$$(call MK_JARS_TO_LIB,$(6))) | $(JARS_DIR)
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

-include $$(call SOURCES_TO_DEPENDENCIES,$(1),$(2),$(4))

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


$(RESOURCES_DIR)/%:
	$(MKDIR_P) $$(dirname $@) && \
	$(SED)$(RESOURCES_FILTER_SCRIPT) $^ >$@


endif # eq ($(BUILD_PHASE),1)


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
include scheduler/build.mk
# Contains EXTRA_VARIABLES
include service/build.mk
# !!! run.jar
include sgw/boot/build.mk
include sgw/gateway/build.mk
include statistics/api/build.mk
include statistics/impl/build.mk
include stream/api/build.mk
include tcap-ansi/tcap-ansi-api/build.mk
include tcap-ansi/tcap-ansi-impl/build.mk
include tcap/tcap-api/build.mk
include tcap/tcap-impl/build.mk
# !!! run.jar
include tools/simulator/bootstrap/build.mk
include tools/simulator/core/build.mk
include tools/simulator/gui/build.mk
include tools/trace-parser/parser/build.mk

include docs/sources/build.mk
# !!! include docs/jdocbook-mobicents/build.mk

ifeq ($(ENABLE_DAHDI),true)
include hardware/dahdi/native/build.mk
endif

ifeq ($(ENABLE_DIALOGIC),true)
include hardware/dialogic/native/build.mk
endif

# !!!
# include release/build.mk

-include $(JAVA_DEPENDENCIES)

ifdef MISSING_RESOURCE_JARS
$(error Missing extra jar(s): $(MISSING_RESOURCE_JARS))
endif

jars: $(JARS_LIST)


ifeq ($(BUILD_PHASE),1)


package: $(bootstrap.buildname) $(smsc-resource-adaptors-du.buildname) $(smsc-services-du.buildname) \
         $(smsc-cli.buildname) $(smpp-simulator.buildname) $(smpp-simulator-bootstrap.buildname)


else #eq ($(BUILD_PHASE),1)


$(PACKAGE_DIR)/%.jar: $(JARS_DIR)/%.jar
	$(MKDIR_P) $$(dirname $@) && \
	$(CP) $^ $@

$(PACKAGE_DIR)/smsc/%.jar: $(JARS_DIR)/%.jar
	$(MKDIR_P) $$(dirname $@) && \
	$(CP) $^ $@

$(PACKAGE_DIR)/smpp-simulator/%.jar: $(JARS_DIR)/%.jar
	$(MKDIR_P) $$(dirname $@) && \
	$(CP) $^ $@

$(PACKAGE_DIR)/smpp-simulator/run.jar: $(smpp-simulator-bootstrap.buildname)
	$(MKDIR_P) $$(dirname $@) && \
	$(CP) $^ $@

package: $(PACKAGE_DIR)/$(smsc-cli.basename) \
         $(PACKAGE_DIR)/smsc/$(bootstrap.basename) \
         $(PACKAGE_DIR)/smsc/$(smsc-resource-adaptors-du.basename) \
         $(PACKAGE_DIR)/smsc/$(smsc-services-du.basename) \
         $(PACKAGE_DIR)/smpp-simulator/$(smpp-simulator.basename) \
         $(PACKAGE_DIR)/smpp-simulator/run.jar


endif # eq ($(BUILD_PHASE),1)


endif # ndef BUILD_PHASE
