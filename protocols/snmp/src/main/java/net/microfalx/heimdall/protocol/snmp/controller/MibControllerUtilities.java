package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.heimdall.protocol.snmp.mib.MibImport;
import net.microfalx.heimdall.protocol.snmp.mib.MibModule;
import net.microfalx.heimdall.protocol.snmp.mib.MibSymbol;
import net.microfalx.heimdall.protocol.snmp.mib.MibVariable;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.Comparator;

class MibControllerUtilities {

    /**
     * Drops various variables related to a MIB module on the model.
     *
     * @param model  the model
     * @param module the MIB module
     */
    static void updateContext(Model model, MibModule module) {
        ArrayList<MibImport> imports = new ArrayList<>(module.getImportedModules());
        imports.sort(Comparator.comparing(MibImport::getName));
        model.addAttribute("imports", imports);
        ArrayList<MibSymbol> symbols = new ArrayList<>(module.getSymbols().stream().filter(s -> !s.isVariable()).toList());
        imports.sort(Comparator.comparing(MibImport::getName));
        model.addAttribute("symbols", symbols);
        ArrayList<MibVariable> variables = new ArrayList<>(module.getVariables());
        imports.sort(Comparator.comparing(MibImport::getName));
        model.addAttribute("variables", variables);
    }
}
