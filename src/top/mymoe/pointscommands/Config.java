package top.mymoe.pointscommands;

import org.black_ixx.playerpoints.services.PointsCommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ginya on 2017/4/11.
 */
public class Config {
    private Plugin plugin;
    private Map<String,PluginCommand> iCommands;
    private FileConfiguration fileConfiguration;
    private PluginCommand confirmCommand;
    public Config(Plugin plugin) {
        this.plugin = plugin;
        iCommands = new HashMap<>();
        this.fileConfiguration=plugin.getConfig();
    }
    public void reload() {
        plugin.reloadConfig();
        this.fileConfiguration=plugin.getConfig();
        Map<String,PluginCommand> tempMap = new HashMap<>();
        try {
            Constructor<?> constructor = PluginCommand.class.getDeclaredConstructor(String.class,Plugin.class);
            constructor.setAccessible(true);
            for(String name:fileConfiguration.getConfigurationSection("PointsCommands").getKeys(false)){
                PluginCommand command = null;
                if(iCommands.containsKey(name)){
                    command = iCommands.get(name);
                }else {
                    command = (PluginCommand) constructor.newInstance(new Object[]{name, plugin});
                }
                if(fileConfiguration.getString("PointsCommands."+name+".description")!=null)
                    command.setDescription(fileConfiguration.getString("PointsCommands."+name+".description"));
                if(fileConfiguration.getStringList("PointsCommands."+name+".aliases")!=null)
                    command.setAliases(fileConfiguration.getStringList("PointsCommands."+name+".aliases"));
                tempMap.put(name,command);
            }
            if (confirmCommand==null||(!confirmCommand.getName().equalsIgnoreCase(fileConfiguration.getString("ConfirmCommandName")))){
                confirmCommand = (PluginCommand) constructor.newInstance(new Object[]{fileConfiguration.getString("ConfirmCommandName"), plugin});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        iCommands = tempMap;
    }

    public PointsPlugins getPointsPlugin() {
        return PointsPlugins.valueOf(fileConfiguration.getString("PointsPlugin","playerpoints").toLowerCase());
    }

    public Map<String,PluginCommand> getICommands() {
        return iCommands;
    }

    public String getCommandMessage(String name,String key){
        return fileConfiguration.getString("PointsCommands."+name+"."+key,fileConfiguration.getString("DefaultMessages."+key,key));
    }

    public int getPoints(String name) {
        return fileConfiguration.getInt("PointsCommands."+name+".Points",0);
    }

    public int getRequiredArgs(String name ){
        return fileConfiguration.getInt("PointsCommands."+name+".RequiredArgs",-1);
    }

    public List<String> getCommandRuns(String name){
        return fileConfiguration.getStringList("PointsCommands."+name+".runcmd");
    }

    public String getCommandPermission(String name) {
        return fileConfiguration.getString("PointsCommands."+name+".Permission","");
    }

    public boolean isConfirmCommands() {
        return fileConfiguration.getBoolean("ConfirmCommands");
    }

    public PluginCommand getConfirmCommand(){
        return  this.confirmCommand;
    }

    public int getConfirmTime() {
        return fileConfiguration.getInt("ConfirmTime");
    }
}
