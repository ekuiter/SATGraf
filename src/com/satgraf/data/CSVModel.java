/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.satgraf.data;

import Jama.Matrix;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author zacknewsham
 */
public class CSVModel{
    private File modelFile;
    private HashMap<String, Integer> headers = new HashMap<>();
    private ArrayList<Object[]> values = new ArrayList<>();
    private ArrayList<Double> max = new ArrayList<>();
    private ArrayList<Double> min = new ArrayList<>();
    
    public CSVModel(String[] names){
        for(int i = 0; i < names.length; i++){
            String name = names[i];
            headers.put(name, i);
        }
    }
    
    public CSVModel(Matrix m, String[] names){
        boolean first = true;
        for(int i = 0; i < names.length; i++){
            String name = names[i];
            headers.put(name, i);
            for(int r = 0; r < m.getRowDimension(); r++){
                if(first){
                    values.add(new Double[names.length]);
                }
                values.get(r)[i] = new Double(m.get(r, i));
            }
            first = false;
        }
    }
    
    public CSVModel(File file) throws FileNotFoundException, IOException{
        this.modelFile = file;
        BufferedReader reader = new BufferedReader(new FileReader(modelFile));
        String line;
        boolean header = true;
        while((line = reader.readLine()) != null){
            if(header){
                String[] headerParts = line.split(",");
                Integer i = 0;
                for(String part: headerParts){
                    headers.put(part.replace("\"",""), i);
                    max.add(new Double(0.0));
                    min.add(new Double(Double.MAX_VALUE));
                    i++;
                }
                header = false;
                continue;
            }
            String[] valueParts = line.split(",");
            Object[] values = new Object[headers.size()];
            int i = 0;
            for(String part : valueParts){
                try{
                    Double value = new Double(part);
                    values[i] = value;
                    //max.set(i, max.get(i).max(values[i]));
                    //min.set(i, min.get(i).min(values[i]));
                    min.set(i, min.get(i) > value ? value : min.get(i));
                    max.set(i, max.get(i) < value ? value : max.get(i));
                }
                catch(NumberFormatException ex){
                    values[i] = part.replace("\"","");
                }
                i++;
            }
            this.values.add(values);
        }
    }
    
    public void addHeader(String header){
        headers.put(header, headers.size());
    }
    
    public Matrix getMatrix(String[] use){
        Matrix m = new Matrix(values.size(), use.length);
        for(int c = 0; c < use.length; c++){
            for(int r = 0; r < values.size(); r++){
                m.set(r, c, ((Double)this.get(r, use[c])).doubleValue());
            }
        }
        return m;
    }
    
    public void completeCases(){
        for(int i = 0; i < values.size(); i++){
            for(int a = 0; a < values.get(i).length; a++){
                if(values.get(i)[a] == null){
                    values.remove(i);
                    i--;
                    break;
                }
            }
        }
    }
    
    public void normalize(){
        for(int col = 0; col < headers.size(); col++){
            Double max = this.max.get(col);
            Double min = this.min.get(col);
            for(Object[] row : this.values){
                //row[col] = (row[col].subtract(min)).divide(max.subtract(min));
                if(row[col] instanceof Double){
                    row[col] = ((Double)row[col] - min) / (max - min);
                }
            }
        }
    }
    
    public void set(int row, String column, Double val){
        this.set(row, headers.get(column), val);
    }
    
    private void set(int row, int column, Double val){
        while(this.values.size() <= row){
            this.values.add(new Double[this.getColumnCount()]);
        }
        Object[] values = this.values.get(row);
        if(values.length <= column){
            Object[] new_values = new Object[column + 1];
            for(int i = 0; i < values.length; i++){
                new_values[i] = values[i];
            }
            this.values.set(row, new_values);
        }
        this.values.get(row)[column] = val;
    }
    
    public Object get(int row, String column){
        if(column.equals("(Intercept)") && !headers.containsKey(column)){
            return 1.0;
        }
        if(column.contains(":") && !headers.containsKey(column)){
            Double x = new Double(1.0);
            String[] names = column.split(":");
            for(String _name : names){
                //x = x.multiply(this.get(row, _name));
                x = x * (Double)this.get(row,_name);
            }
            return x;
        }
        else{
            try{
            return this.get(row, headers.get(column));
            }
            catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }
    
    private Object get(int row, int column){
        return values.get(row)[column];
    }
    
    public Set<String> getHeaders(){
        return headers.keySet();
    }
    
    public int getRowCount(){
        return values.size();
    }
    
    public int getColumnCount(){
        return headers.size();
    }
    
    public int removeRowsBelow(String name, Double value){
        return this.removeRowsBelow(headers.get(name), value);
    }
    
    private int removeRowsBelow(int column, Double value){
        int removed = 0;
        for(int i = 0; i < values.size(); i++){
            if(value > (Double)values.get(i)[column]){
                values.remove(i);
                i--;
                removed ++;
            }
        }
        return removed;
    }
    
    public int removeRowsWith(String name, Double value){
        return this.removeRowsWith(headers.get(name), value);
    }
    
    private int removeRowsWith(int column, Double value){
        int removed = 0;
        for(int i = 0; i < values.size(); i++){
            if(value.equals(values.get(i)[column])){
                values.remove(i);
                i--;
                removed ++;
            }
        }
        return removed;
    }
    public String toString(String[] names){
        StringBuilder ret = new StringBuilder();
        ret.append("row\t");
        for(String header: names){
            ret.append(String.format("%s\t",header));
        }
        ret.append("\n");
        int i = 1;
        for(Object[] row : values){
            ret.append(String.format("%d\t",i));
            for(String header: names){
                if(row.length <= headers.get(header)){
                    ret.append("NA\t\t");
                }
                else if(row[headers.get(header)] == null){
                    ret.append("NA\t\t");
                }
                else if(row[headers.get(header)] instanceof String){
                    ret.append(row[headers.get(header)]);
                    ret.append("\t");
                }
                else{
                    ret.append(String.format("%f\t",row[headers.get(header)]));
                }
            }
            ret.append("\n");
            i++;
        }
        return ret.toString();
        
    }
    
    public String toString(){
        return this.toString((String[])headers.keySet().toArray());
    }
    public String[] getOrderedHeaders(){
        String[] ret = new String[headers.size()];
        for(String header: headers.keySet()){
            ret[headers.get(header)] = header;
        }
        return ret;
    }
    public void toFile(File f) throws IOException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        StringBuilder ret = new StringBuilder();
        for(String header: getOrderedHeaders()){
            ret.append(String.format("\"%s\",",header));
        }
        ret.delete(ret.length() - 1, ret.length());
        ret.append("\n");
        for(Object[] row : values){
            for(String header: getOrderedHeaders()){
                if(row.length > headers.get(header)){
                    ret.append(String.format("%s,",String.valueOf(row[headers.get(header)])));
                }
            }
            ret.delete(ret.length() - 1, ret.length());
            ret.append("\n");
        }
        bw.write(ret.toString());
        bw.close();
    }
}
