package top.mymoe.pointscommands;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by ginya on 2017/4/11.
 */
public class PointsCommands extends JavaPlugin {

    public static Config config;
    public static Plugin plugin;
    public static Logger logger;
    public static PointsApi pointsApi;
    public static String name ="PointsCommands";
    private boolean placeholderAPIenable;

    private Map<String,PluginCommand> commands;

    @Override
    public void onEnable() {
        plugin = this;
        logger = this.getLogger();
        this.saveDefaultConfig();
        if(config == null)
            config = new Config(this);
        config.reload();
        if(pointsApi == null)
            pointsApi = new PointsApi(this);
        pointsApi.reload();
        commands=config.getICommands();
        registerCommands();
        placeholderAPIenable =this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(sender.toString()+command.toString()+label+args.toString());
        if(command.getName().equalsIgnoreCase("PointsCommands")){
            if(sender.hasPermission("pointscommands.commands")){
                if(args.length==0){
                    sender.sendMessage("PointsCommands, Use:'/PointsCommands help' for help.");
                    return true;
                }
                if(args[0].equalsIgnoreCase("help")){
                    sender.sendMessage("help:Show helps\nreload:Reload this plugin");
                    return true;
                }
                if(args[0].equalsIgnoreCase("reload")){
                    config.reload();
                    pointsApi.reload();
                    commands=config.getICommands();
                    registerCommands();
                    sender.sendMessage("PointsCommands reloaded.");
                    return true;
                }
            }
            return true;
        }
        if(commands.containsValue(command)){
            if(!(sender instanceof Player)){
                sender.sendMessage("This command can only be used as a player");
                return true;
            }
            String name = command.getName();
            if(!sender.hasPermission(config.getCommandPermission(name))){
                sender.sendMessage(config.getCommandMessage(name,"NoPermissionMessage"));
            }
            if((config.getRequiredArgs(name) != -1)&&(args.length!=config.getRequiredArgs(name))){
                sender.sendMessage(config.getCommandMessage(name,"WrongArgsMessage"));
                return true;
            }
            List<String> runCmds = new LinkedList<>();
            for (String cmd:config.getCommandRuns(name)){
                if(placeholderAPIenable)
                    cmd = PlaceholderAPI.setPlaceholders((Player)sender,cmd);
                cmd = cmd.replaceAll("\\$player",sender.getName());
                int k = 0;
                for(int i=0;i<9;i++){
                    if(cmd.contains("$arg"+(i+1))){
                        k=i;
                        if(args.length+1<i){
                            sender.sendMessage(config.getCommandMessage(name,"WrongArgsMessage"));
                            return true;
                        }else {
                            cmd = cmd.replaceAll("\\$arg"+(i+1),args[i]);
                        }
                    }
                }
                if(cmd.contains("$multiargs")){
                    StringBuffer multiArgs = new StringBuffer();
                    for(int i=k;i<args.length;i++){
                        multiArgs.append(" "+args[i]);
                    }
                    cmd = cmd.replaceAll("\\$multiargs", multiArgs.substring(Math.min(1,multiArgs.length())));
                }
                runCmds.add(cmd);
            }
            if(config.getPoints(name)!=0){
                if(!pointsApi.take((Player)sender,config.getPoints(name),command.getName())) {
                    sender.sendMessage(config.getCommandMessage(name, "NoPointsMessage"));
                    return true;
                }
            }
            for (String cmd:runCmds){
                if(cmd.startsWith("op:")){
                    cmd = cmd.replaceFirst("op:","");
                    boolean opSet = sender.isOp();
                    sender.setOp(true);
                    ((Player)sender).chat("/"+cmd);
                    sender.setOp(opSet);
                }else if(cmd.startsWith("console:")){
                    cmd = cmd.replaceFirst("console:","");
                    this.getServer().dispatchCommand(this.getServer().getConsoleSender(),cmd);
                }else{
                    ((Player)sender).chat("/"+cmd);
                }
            }
            sender.sendMessage(config.getCommandMessage(name,"SuccessMessage"));
            return true;
        }
        return false;
    }

    private void unRegisterCommands(){

    }

    private void registerCommands() {
        try {
            Field field = SimplePluginManager.class.getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap)field.get(Bukkit.getPluginManager());
            for(PluginCommand command:commands.values()){
                if(!command.isRegistered()){
                    commandMap.register(this.name,command);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
