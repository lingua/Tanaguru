/*
 * Tanaguru - Automated webpage assessment
 * Copyright (C) 2008-2011  Open-S Company
 *
 * This file is part of Tanaguru.
 *
 * Tanaguru is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact us by mail: open-s AT open-s DOT com
 */
package org.opens.tgol.form.parameterization.helper;

import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.opens.tanaguru.entity.reference.Reference;
import org.opens.tanaguru.entity.service.reference.ReferenceDataService;
import org.opens.tgol.entity.option.OptionElement;
import org.opens.tgol.form.NumericalFormField;
import org.opens.tgol.form.SelectElement;
import org.opens.tgol.form.SelectFormField;
import org.opens.tgol.form.TextualFormField;
import org.opens.tgol.form.parameterization.AuditSetUpFormField;

/**
 * That class handles utility methods (activation/desactivation, restriction application)
 * to deal with AuditSetUpFormFields. It also manages specific AuditSetUpFormFields 
 * that deal with the level selection.
 * 
 * @author jkowalczyk
 */
public final class AuditSetUpFormFieldHelper {

    private static Map<String, String> DEFAULT_LEVEL_BY_REF_MAP = Collections.EMPTY_MAP;
    public static void setDefaultLevelByRefMap(Map<String, String> defaultLevelByRef) {
        DEFAULT_LEVEL_BY_REF_MAP = defaultLevelByRef;
    }
     
    public static void setReferenceDataService(ReferenceDataService referenceDataService) {
        DEFAULT_LEVEL_BY_REF_MAP = new HashMap<String, String> ();
        for (Reference reference : referenceDataService.findAll()) {
            DEFAULT_LEVEL_BY_REF_MAP.put(reference.getCode(), reference.getDefaultLevel().getCode());
        }
    }
    
    private AuditSetUpFormFieldHelper() {
    }

    /**
     * A restriction can be applied to an AuditSetUpFormField when an option
     * matches with a parameter (by its code). In this case, regarding the type
     * of the FormField, the value of the option override the default
     *
     * @param ap
     * @param optionElementSet
     */
    public static void applyRestrictionToAuditSetUpFormField(
            AuditSetUpFormField ap,
            Collection<OptionElement> optionElementSet) {
        if (ap.getFormField() instanceof NumericalFormField) {
            OptionElement optionElement = getOptionFromOptionSet(
                    optionElementSet,
                    ap.getParameterElement().getParameterElementCode());
            if (optionElement != null) {
                ((NumericalFormField) ap.getFormField()).setMaxValue(optionElement.getValue());
                ((NumericalFormField) ap.getFormField()).setValue(optionElement.getValue());
            }
        } else if (ap.getFormField() instanceof SelectFormField) {
            activateSelectFormField((SelectFormField) ap.getFormField(), optionElementSet);
        } else if (ap.getFormField() instanceof TextualFormField) {
            OptionElement optionElement = getOptionFromOptionSet(
                    optionElementSet,
                    ap.getParameterElement().getParameterElementCode());
            if (optionElement != null) {
                ((TextualFormField) ap.getFormField()).setValue(optionElement.getValue());
            }
        }
    }

    /**
     *
     * @param optionElementSet
     * @param optionCode
     * @return
     */
    private static OptionElement getOptionFromOptionSet(
            Collection<OptionElement> optionElementSet,
            String optionCode) {
        
        for (OptionElement optionElement : optionElementSet) {
            if (StringUtils.equals(optionCode, optionElement.getOption().getCode())) {
                return optionElement;
            }
        }
        return null;
    }

    /**
     * This method enables/disables a select form field element regarding the
     * activation / restriction strategy. If a SelectFormField is set as enabled
     * by default and has a set restrictionCode, it can be disabled. If a
     * SelectFormField is set as disabled by default and has a set
     * activationCode, it can be enabled.
     *
     * @param selectFormField
     * @param optionElementSet
     */
    private static void activateSelectFormField(
            SelectFormField selectFormField,
            Collection<OptionElement> optionElementSet) {
        boolean enableElements = true;
        OptionElement optionElement = null;
        if (StringUtils.isNotEmpty(selectFormField.getRestrictionCode())) {
            optionElement = getOptionFromOptionSet(
                    optionElementSet,
                    selectFormField.getRestrictionCode());
            enableElements = false;
        } else if (StringUtils.isNotEmpty(selectFormField.getActivationCode())) {
            optionElement = getOptionFromOptionSet(
                    optionElementSet,
                    selectFormField.getActivationCode());
        }
        if (optionElement != null) {
            String[] optionValues = optionElement.getValue().split(";");
            boolean isSelectFormFieldHasDefault = false;
            for (int i = 0; i < optionValues.length; i++) {
                List<SelectElement> sel =
                        selectFormField.getSelectElementMap().get(optionValues[i]);
                for (SelectElement se : sel) {
                    se.setEnabled(enableElements);
                    // By option mechanism, we can set an SelectElement as default.
                    if (!isSelectFormFieldHasDefault) {
                        isSelectFormFieldHasDefault = setSelectElementAsDefault(se, optionElementSet);
                    }
                }
            }
            // When no default elements have been found, the first enabled element
            // is set as default
            if (!isSelectFormFieldHasDefault) {
                setFirstSelectElementAsDefault(selectFormField.getSelectElementMap());
            }
        }
    }

    /**
     * A select element can be set as default by option settings.
     *
     *
     * @param selectElement
     * @param optionElementSet
     * @return
     */
    private static boolean setSelectElementAsDefault(
            SelectElement selectElement,
            Collection<OptionElement> optionElementSet) {
        OptionElement optionElement = getOptionFromOptionSet(
                optionElementSet,
                selectElement.getDefaultCode());
        if (optionElement == null) {
            return false;
        }
        if (selectElement.getEnabled()
                && StringUtils.equals(optionElement.getValue(), selectElement.getValue())) {
            selectElement.setDefault(true);
            return true;
        }
        return false;
    }

    /**
     * When no SelectElement is defined as default from a list whole list, we
     * set the first element of the list as default.
     *
     * @param sel
     */
    private static void setFirstSelectElementAsDefault(Map<String, List<SelectElement>> selElementMap) {
        boolean firstEnabledElementEncountered = false;
        for (List<SelectElement> seList : selElementMap.values()) {
            for (SelectElement se : seList) {
                if (!firstEnabledElementEncountered && se.getEnabled()) {
                    se.setDefault(true);
                    firstEnabledElementEncountered = true;
                } else {
                    se.setDefault(false);
                }
            }
        }
    }

    /**
     * This very specific method is used to enable/disable the appropriate
     * referential in a SelectFormField regarding a set of allowed referentials.
     *
     * @param refAndLevelSelectFormField
     * @param allowedReferentialCodeSet
     */
    public static void activateAllowedReferentialField(
            SelectFormField refAndLevelSelectFormField,
            Collection<String> allowedReferentialCodeSet) {
        for (Map.Entry<String, List<SelectElement>> refKey : refAndLevelSelectFormField.getSelectElementMap().entrySet()) {
            if (allowedReferentialCodeSet.contains(refKey.getKey())) {
                enableSelectElement(true, refKey.getValue());
            } else{
                enableSelectElement(false, refKey.getValue());
            }
        }

    }

    /**
     * Enable / disable a set of selectElement
     * 
     * @param enableElement
     * @param sel 
     */
    private static void enableSelectElement(boolean enableElement, List<SelectElement> sel) {
        for (SelectElement se : sel) {
            se.setEnabled(enableElement);
        }
    }

    /**
     * 
     * @param selectFormFieldList
     * @param ref 
     */
    public static void selectDefaultLevelFromRefValue(
                    List<SelectFormField> selectFormFieldList, 
                    String ref) {
        StringBuilder strb = new StringBuilder();
        strb.append(ref);
        strb.append(";");
        if (DEFAULT_LEVEL_BY_REF_MAP.containsKey(ref)) {
            strb.append(DEFAULT_LEVEL_BY_REF_MAP.get(ref));
        }
        selectDefaultLevelFromLevelValue(selectFormFieldList, strb.toString());
    }

    /**
     * 
     * @param selectFormFieldList
     * @param defaultLevel 
     */
    public static void selectDefaultLevelFromLevelValue (
             List<SelectFormField> selectFormFieldList, 
             String defaultLevel) {
        for (SelectFormField sff : selectFormFieldList) {
            for (List<SelectElement> entry : sff.getSelectElementMap().values()) {
                setLevelElementAsDefault(entry, defaultLevel);
            }
        }
    }       
    
    /**
     * Only one level Element can be set as Default. 
     *
     *
     * @param selectElementList
     * @param defaultLevel
     * @return
     */
    private static void setLevelElementAsDefault (
            List<SelectElement> selectElementList,
            String defaultLevel) {
        for (SelectElement se : selectElementList) {
            se.setDefault(false);
            if (se.getEnabled()
                    && StringUtils.equals(defaultLevel, se.getValue())) {
                se.setDefault(true);
            }
        }
    }
    
}