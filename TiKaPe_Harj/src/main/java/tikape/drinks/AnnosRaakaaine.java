/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tikape.drinks;

/**
 *
 * @author Martin
 */
public class AnnosRaakaaine {
    
    private String raakaaine;
    private Integer annos_id;
    private Integer jarjestys;
    private String maara;
    private String ohje;


    public AnnosRaakaaine(String raakaaine, Integer annos_id, Integer jarjestys, String maara, String ohje) {
        this.raakaaine = raakaaine;
        this.annos_id = annos_id;
        this.jarjestys = jarjestys;
        this.maara = maara;
        this.ohje = ohje;
    }
    
    public String getRaakaaine() {
        return raakaaine;
    }

    public Integer getAnnosid() {
        return annos_id;
    }
    
    public Integer getJarjestys() {
        return jarjestys;
    }
    
    public String getMaara() {
        return maara;
    }
    
    public String getOhje() {
        return ohje;
    }
}
