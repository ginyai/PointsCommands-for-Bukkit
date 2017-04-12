package top.mymoe.pointscommands;

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
    private Map<PluginCommand,String> iCommands;
    private FileConfiguration fileConfiguration;
    public Config(Plugin plugin) {
        this.plugin = plugin;
        iCommands = new HashMap<>();
        this.fileConfiguration=plugin.getConfig();
    }
    public void reload() {
        plugin.reloadConfig();
        this.fileConfiguration=plugin.getConfig();
        iCommands.clear();
        try {
            Constructor<?> constructor = PluginCommand.class.getDeclaredConstructor(String.class,Plugin.class);
            constructor.setAccessible(true);
            for(String name:fileConfiguration.getConfigurationSection("PointsCommands").getKeys(false)){
                PluginCommand command = (PluginCommand) constructor.newInstance(new Object[]{name,plugin});
                if(fileConfiguration.getString("PointsCommands."+name+".description")!=null)
                    command.setDescription(fileConfiguration.getString("PointsCommands."+name+".description"));
                if(fileConfiguration.getStringList("PointsCommands."+name+".aliases")!=null)
                    command.setAliases(fileConfiguration.getStringList("PointsCommands."+name+".aliases"));
                iCommands.put(command,name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PointsPlugins getPointsPlugin() {
        return PointsPlugins.valueOf(fileConfiguration.getString("PointsPlugin","playerpoints").toLowerCase());
    }

    public Map<PluginCommand,String> getICommands() {
        return iCommands;
    }

    public String getCommandMessage(String name,String key){
        return fileConfiguration.getString("PointsCommands."+name+"."+key,key);
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
}
