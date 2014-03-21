/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visual.evolution;
import gnu.trove.map.hash.TObjectCharHashMap;
import java.text.DecimalFormat;
import javax.swing.JOptionPane;
import visual.community.CNMCommunityMetric;
import visual.community.CommunityEdge;
import visual.community.CommunityGraph;
import visual.community.CommunityMetric;
import visual.community.CommunityNode;
import visual.community.ConcreteCommunityGraph;
import visual.community.OLCommunityMetric;

/**
 *
 * @author zacknewsham
 */
public class EvolutionGenerator extends javax.swing.JDialog {

  private EvolutionGrapher grapher;
  /**
   * Creates new form EvolutionGenerator
   */
  public EvolutionGenerator(){
    initComponents();
  }
  public EvolutionGenerator(EvolutionGrapher grapher) {
    super(grapher.getFrame(), true);
    this.grapher = grapher;
    initComponents();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    btnGenerate = new javax.swing.JButton();
    btnCancel = new javax.swing.JButton();
    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    jLabel4 = new javax.swing.JLabel();
    txtVariables = new javax.swing.JTextField();
    txtClauses = new javax.swing.JTextField();
    txtCommunities = new javax.swing.JTextField();
    sldrModularity = new javax.swing.JSlider();
    txtModularity = new javax.swing.JTextField();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

    btnGenerate.setText("Generate");
    btnGenerate.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnGenerateActionPerformed(evt);
      }
    });

    btnCancel.setText("Cancel");
    btnCancel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnCancelActionPerformed(evt);
      }
    });

    jLabel1.setText("# Variables");

    jLabel2.setText("# Clauses");

    jLabel3.setText("# Communities");

    jLabel4.setText("Modularity");

    txtVariables.setText("500");
    txtVariables.setName("txtVariables"); // NOI18N

    txtClauses.setText("2000");
    txtClauses.setName("txtClauses"); // NOI18N

    txtCommunities.setText("50");
    txtCommunities.setName("txtCommunities"); // NOI18N

    sldrModularity.setMinimum(1);
    sldrModularity.setMinorTickSpacing(1);
    sldrModularity.setName("sldModularity"); // NOI18N
    sldrModularity.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        sldrModularityStateChanged(evt);
      }
    });

    txtModularity.setText("0.5");
    txtModularity.setName("txtModularity"); // NOI18N

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGap(0, 1, Short.MAX_VALUE)
            .addComponent(btnCancel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btnGenerate))
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel4)
            .addGap(0, 0, Short.MAX_VALUE))
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(jLabel3)
              .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(txtVariables)
              .addComponent(txtClauses)
              .addComponent(txtCommunities)))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(sldrModularity, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGap(18, 18, 18)
            .addComponent(txtModularity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(txtVariables, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(18, 18, 18)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(txtClauses, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(18, 18, 18)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel3)
          .addComponent(txtCommunities, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(18, 18, 18)
        .addComponent(jLabel4)
        .addGap(18, 18, 18)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(sldrModularity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(txtModularity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(btnGenerate)
          .addComponent(btnCancel))
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void btnGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateActionPerformed
    int vars = 0;
    int clauses = 0;
    int coms = 0;
    double target_mod = 0.0;
    try{
      vars = Integer.parseInt(txtVariables.getText());
      clauses = Integer.parseInt(txtClauses.getText());
      coms = Integer.parseInt(txtCommunities.getText());
    }
    catch(NumberFormatException e){
      JOptionPane pane = new JOptionPane("All of Variables, Clauses and Communities must be integers", JOptionPane.ERROR);
      pane.setVisible(true);
      return;
    }
    try{
      target_mod = Double.parseDouble(txtModularity.getText());
    }
    catch(NumberFormatException e){
      JOptionPane pane = new JOptionPane("Modularity should be a double", JOptionPane.ERROR);
      pane.setVisible(true);
      return;
    }
    CommunityGraph cg = makeCommunity(vars, clauses, coms, target_mod);
    grapher.process(cg);
    this.setVisible(false);
  }//GEN-LAST:event_btnGenerateActionPerformed

  private static int getLitBetween(int low, int high){
    int ret = getRandomBetween(low, high);
    
    boolean pos = Math.random() >= 0.5;
    return pos ? ret : -ret;
  }
  private static int getRandomBetween(int low, int high){
    return low + (int)Math.round(Math.random() * (high - low));
  }
  
  private static int getLitOutside(int low, int high, int max){
    boolean first = Math.random() >= 0.5;
    if(first && low > 0){
      return getLitBetween(0, low);
    }
    else{
      return getLitBetween(high, max);
    }
  }
  private static int[] makeClause(int vars_count, int coms_count, double q){
    int cmty = getRandomBetween(1, coms_count);
    int cmtySize = (int)Math.round((double)vars_count / (double)coms_count);
    
    int a = 0;
    while(a == 0 || a > vars_count){
      a = getLitBetween(1, cmtySize) * coms_count + cmty;
    }
    
    int b = 0;
    while(b == 0 || b > vars_count){
      double r1 = Math.random();
      b = r1 < q ? getLitBetween(0, cmtySize) * coms_count + cmty : getLitBetween(1, vars_count);
    }
    int c = 0;
    while(c == 0 || c > vars_count){
      double r2 = Math.random();
      c = r2 < q ? getLitBetween(0, cmtySize) * coms_count + cmty : getLitBetween(1, vars_count);
    }
    return new int[]{a, b, c};//String.format("%d %d %d 0\n", a, b, c);
  }
  public static CommunityGraph makeCommunity(int vars_count, int clauses_count, int coms_count, double q){
    CommunityGraph cg = new ConcreteCommunityGraph();
    while(cg.getClausesCount() < clauses_count){
      int[] clause = makeClause(vars_count, coms_count, q);
      boolean _a = clause[0] > 0;
      boolean _b = clause[1] > 0;
      boolean _c = clause[2] > 0;
      if(!_a){
        clause[0] = 0 - clause[0];
      }
      if(!_b){
        clause[1] = 0 - clause[1];
      }
      if(!_c){
        clause[2] = 0 - clause[2];
      }
      TObjectCharHashMap<CommunityNode> nodes = new TObjectCharHashMap<CommunityNode>();
      CommunityNode a = cg.createNode(clause[0], null);
      CommunityNode b = cg.createNode(clause[1], null);
      CommunityNode c = cg.createNode(clause[2], null);
      
      
      nodes.put(a, _a ? '1' : '0');
      nodes.put(b, _b ? '1' : '0');
      nodes.put(c, _c ? '1' : '0');
      cg.createClause(nodes);
      
      CommunityEdge ab = cg.createEdge(a, b, false);
      CommunityEdge ac = cg.createEdge(a, c, false);
      CommunityEdge bc = cg.createEdge(b, c, false);
      
      a.addEdge(ab);
      a.addEdge(ac);
      b.addEdge(ab);
      b.addEdge(bc);
      c.addEdge(ac);
      c.addEdge(bc);
    }
    CommunityMetric cm = new OLCommunityMetric();
    
    double mod = cm.getCommunities(cg);
    return cg;
  }
  private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
    this.setVisible(false);
  }//GEN-LAST:event_btnCancelActionPerformed

  private void sldrModularityStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldrModularityStateChanged
    txtModularity.setText(new DecimalFormat("#.##").format((double)sldrModularity.getValue() / (double)100));
  }//GEN-LAST:event_sldrModularityStateChanged

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
     */
    try {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (ClassNotFoundException ex) {
      java.util.logging.Logger.getLogger(EvolutionGenerator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(EvolutionGenerator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(EvolutionGenerator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(EvolutionGenerator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
        //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        new EvolutionGenerator().setVisible(true);
      }
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnCancel;
  private javax.swing.JButton btnGenerate;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JSlider sldrModularity;
  private javax.swing.JTextField txtClauses;
  private javax.swing.JTextField txtCommunities;
  private javax.swing.JTextField txtModularity;
  private javax.swing.JTextField txtVariables;
  // End of variables declaration//GEN-END:variables
}
