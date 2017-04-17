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
    private Map<Player,UnconfirmedCommand> unconfirmedCommands;
    private PluginCommand confirmedCommand;

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
        commands = config.getICommands();
        confirmedCommand = config.getConfirmCommand();

        registerCommands();
        placeholderAPIenable =this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
                    confirmedCommand=config.getConfirmCommand();
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
            String commandName = command.getName();
            if(!(config.getCommandPermission(commandName).equalsIgnoreCase("")||sender.hasPermission(config.getCommandPermission(commandName)))){
                sender.sendMessage(getFormat(config.getCommandMessage(commandName,"NoPermissionMessage"),(Player)sender,commandName,args));
                return true;
            }
            if((config.getRequiredArgs(commandName) != -1)&&(args.length!=config.getRequiredArgs(commandName))){
                sender.sendMessage(getFormat(config.getCommandMessage(commandName,"WrongArgsMessage"),(Player)sender,commandName,args));
                return true;
            }
            List<String> runCmds = new LinkedList<>();
            for (String cmd:config.getCommandRuns(commandName)){
                cmd = getFormat(cmd,(Player)sender,commandName,args);
                if(cmd == null){
                    sender.sendMessage(getFormat(config.getCommandMessage(commandName,"WrongArgsMessage"),(Player)sender,commandName,args));
                    return true;
                }else {
                    runCmds.add(cmd);
                }
            }
            if(config.getPoints(commandName)!=0){
                if(pointsApi.look((Player)sender) < config.getPoints(commandName)) {
                    sender.sendMessage(getFormat(config.getCommandMessage(commandName, "NoPointsMessage"),(Player)sender,commandName,args));
                    return true;
                }
            }
            UnconfirmedCommand unconfirmedCommand = new UnconfirmedCommand(System.currentTimeMillis(),commandName,runCmds,args,(Player)sender);
            if(config.isConfirmCommands()){
                unconfirmedCommands.put((Player)sender,unconfirmedCommand);
            }else {
                unconfirmedCommand.run();
            }
            return true;
        }
        if(command == confirmedCommand){
            if(!(sender instanceof Player)){
            sender.sendMessage("This command can only be used as a player");
            return true;
           }
            UnconfirmedCommand unconfirmedCommand = unconfirmedCommands.get((Player)sender);
            if(unconfirmedCommand==null||unconfirmedCommand.time+config.getConfirmTime()*1000<System.currentTimeMillis()){
                sender.sendMessage(config.getCommandMessage("","NoUnconfirmCommands"));
            }else {
                unconfirmedCommand.run();
            }
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

    private String getFormat(String string,Player player,String commandName,String[] args){
        if(placeholderAPIenable)
            string = PlaceholderAPI.setPlaceholders(player,string);
        string = string.replaceAll("\\$player",player.getName());
        string = string.replaceAll("\\$points",String.valueOf(config.getPoints(commandName)));
        int k = 0;
        for(int i=0;i<9;i++){
            if(string.contains("$arg"+(i+1))){
                k=i;
                if(args.length+1<i){
                    return null;
                }else {
                    string = string.replaceAll("\\$arg"+(i+1),args[i]);
                }
            }
        }
        if(string.contains("$multiargs")){
            StringBuffer multiArgs = new StringBuffer();
            for(int i=k;i<args.length;i++){
                multiArgs.append(" "+args[i]);
            }
            string = string.replaceAll("\\$multiargs", multiArgs.substring(Math.min(1,multiArgs.length())));
        }
        return string;
    }
    private class UnconfirmedCommand{
        private long time;
        private String commandName;
        private List<String> cmds;
        private String[] args;
        private Player player;
        UnconfirmedCommand(long time,String commandName,List<String> cmds,String[] args,Player player){
            this.commandName =commandName;
            this.time =time;
            this.cmds = cmds;
            this.player = player;
        }
        protected void run(){
            if(config.getPoints(commandName)!=0){
                if(!pointsApi.take(player,config.getPoints(commandName),commandName)) {
                    player.sendMessage(getFormat(config.getCommandMessage(commandName, "NoPointsMessage"),player,commandName,args));
                    return ;
                }
            }
            for (String cmd:cmds){
                if(cmd.startsWith("op:")){
                    cmd = cmd.replaceFirst("op:","");
                    boolean opSet = player.isOp();
                    player.setOp(true);
                    player.chat("/"+cmd);
                    player.setOp(opSet);
                }else if(cmd.startsWith("console:")){
                    cmd = cmd.replaceFirst("console:","");
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),cmd);
                }else{
                    player.chat("/"+cmd);
                }
            }
            player.sendMessage(getFormat(config.getCommandMessage(commandName,"SuccessMessage"),player,commandName,args));
        }
        protected String getCommandName() {
            return commandName;
        }
    }
}
