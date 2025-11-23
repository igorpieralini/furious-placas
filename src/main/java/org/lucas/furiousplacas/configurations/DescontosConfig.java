package org.lucas.furiousplacas.configurations;

import org.lucas.furiousplacas.FuriousPlacas;

import java.util.List;
import java.util.Map;

public class DescontosConfig extends Config {
    public DescontosConfig(FuriousPlacas plugin, String fileName) {
        super(plugin, fileName);
    }

    public List<Map<String, Double>> getDescontosVenda() {
        return (List<Map<String, Double>>) getCustomConfig().getList("VENDA");
    }

    public List<Map<String, Double>> getDescontosCompra() {
        return (List<Map<String, Double>>) getCustomConfig().getList("COMPRA");
    }

}
