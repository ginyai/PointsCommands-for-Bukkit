package top.mymoe.pointscommands;

import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Created by ginya on 2017/4/11.
 */
public class PointsApi {
    private Plugin plugin;
    private PlayerPoints playerPoints;
    private PointsPlugins active;
    public PointsApi(Plugin plugin){
        this.plugin = plugin;
    }
    public int look(Player player){
        if(this.active == null){
            PointsCommands.logger.info("No points plugin hocked");
            player.sendMessage("No points plugin hocked");
        }else {
            switch (active){
                case playerpoints:
                    return playerPoints.getAPI().look(player.getUniqueId());
                case mcrmb:
                    return com.mcrmb.PayApi.look(player.getName());
                default:
                    PointsCommands.logger.info("Unknown points plugin hocked");
                    player.sendMessage("Unknown points plugin hocked");
            }
        }
        return 0
                ;
    }
    public boolean take(Player player,int points,String reason){
        if(this.active == null){
            PointsCommands.logger.info("No points plugin hocked");
            player.sendMessage("No points plugin hocked");
        }else {
            switch (active){
                case playerpoints:
                    return playerPoints.getAPI().take(player.getUniqueId(),points);
                case mcrmb:
                    return com.mcrmb.PayApi.Pay(player.getName(),String.valueOf(points),reason,false);
                default:
                    PointsCommands.logger.info("Unknown points plugin hocked");
                    player.sendMessage("Unknown points plugin hocked");
            }
        }
        return false;
    }
    public boolean reload(){
        this.active = null;
        PointsPlugins pointsPlugin = PointsCommands.config.getPointsPlugin();
        if(pointsPlugin==null){
            PointsCommands.logger.info("No points plugin appointed");
        }else {
            switch (pointsPlugin){
                case playerpoints:
                    if(hookPlayerPoints()){
                        this.active = PointsPlugins.playerpoints;
                        PointsCommands.logger.info("playerpoints hocked");
                    }else{
                        PointsCommands.logger.info("Failed to hocked playerpoints");
                    }
                    break;
                case mcrmb:
                    if (plugin.getServer().getPluginManager().isPluginEnabled("Mcrmb")){
                        this.active = PointsPlugins.mcrmb;
                        PointsCommands.logger.info("Mcrmb found");
                    }else {
                        PointsCommands.logger.info("Can not found Mcrmb");
                    }
                    break;
                default:
                    PointsCommands.logger.info("Unknown points plugin appointed");
            }
        }
        return false;
    }
    private boolean hookPlayerPoints(){
        if(!plugin.getServer().getPluginManager().isPluginEnabled("PlayerPoints"))
            return false;
        final Plugin playerPointsPlugin = this.plugin.getServer().getPluginManager().getPlugin("PlayerPoints");
        playerPoints = PlayerPoints.class.cast(playerPointsPlugin);
        return playerPoints != null;
    }
}
