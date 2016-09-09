/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.view.actor;

import name.huliqing.core.ui.tiles.ColumnBody;
import name.huliqing.core.ui.tiles.ColumnText;
import name.huliqing.core.ui.tiles.ColumnIcon;
import name.huliqing.core.Factory;
import name.huliqing.core.constants.InterfaceConstants;
import name.huliqing.core.data.TalentData;
import name.huliqing.core.mvc.service.PlayService;
import name.huliqing.core.view.SimpleRow;
import name.huliqing.core.manager.ResourceManager;
import name.huliqing.core.object.talent.Talent;
import name.huliqing.core.ui.ListView;

/**
 * 
 * @author huliqing
 */
public class TalentRow extends SimpleRow<Talent> {
    protected PlayService playService = Factory.get(PlayService.class);
    
    protected Talent data;
    
    // 物品
    protected ColumnIcon icon;
    protected ColumnBody body;
    protected ColumnText num;
    protected ColumnIcon shortcut;
    
    public TalentRow(ListView parent) {
        super(parent);
        this.setLayout(Layout.horizontal);
        icon = new ColumnIcon(height, height, InterfaceConstants.UI_MISS);
        body = new ColumnBody(height, height, "", "");
        num = new ColumnText(height, height, "");
        shortcut = new ColumnIcon(height, height, InterfaceConstants.UI_UP);
        addView(icon);
        addView(body);
        addView(num);
        addView(shortcut);
    }
    
    @Override
    public void updateViewChildren() {
        super.updateViewChildren();
        float iconSize = height;

        icon.setWidth(iconSize);
        icon.setHeight(iconSize);

        num.setWidth(iconSize);
        num.setHeight(iconSize);

        shortcut.setWidth(iconSize);
        shortcut.setHeight(iconSize);
        shortcut.setPreventEvent(true);

        body.setWidth(width - iconSize * 3);
        body.setHeight(iconSize);
    }

    @Override
    public final void displayRow(Talent data) {
        this.data = data;
        display(this.data);
    }
    
    public Talent getData() {
        return this.data;
    }
    
    public void setRowClickListener(Listener listener) {
        addClickListener(listener);
    }
    
    public void setUpListener(Listener listener) {
        shortcut.addClickListener(listener);
    }
    
    protected void display(Talent talent) {
        TalentData td = talent.getData();
        icon.setIcon(td.getIcon());
        body.setNameText(ResourceManager.getObjectName(td));
        body.setDesText(ResourceManager.getObjectDes(td.getId()));
        num.setText(talent.getLevel() + "/" + talent.getMaxLevel());
    }
}
