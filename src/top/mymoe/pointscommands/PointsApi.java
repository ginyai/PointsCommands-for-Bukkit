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
                default:
                    PointsCommands.logger.info("Unknown points plugin hocked");
                    player.sendMessage("Unknown points plugin hocked");
            }
        }
        return 0
                ;
    }
    public boolean take(Player player,int points){
        if(this.active == null){
            PointsCommands.logger.info("No points plugin hocked");
            player.sendMessage("No points plugin hocked");
        }else {
            switch (active){
                case playerpoints:
                    return playerPoints.getAPI().take(player.getUniqueId(),points);
                default:
                    PointsCommands.logger.info("Unknown points plugin hocked");
                    player.sendMessage("Unknown points plugin hocked");
            }
        }
        return false;
    }
    public boolean reload(){
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
                default:
                    PointsCommands.logger.info("Unknown points plugin appointed");
            }
        }
        return false;
    }
    private boolean hookPlayerPoints(){
        final Plugin playerPointsPlugin = this.plugin.getServer().getPluginManager().getPlugin("PlayerPoints");
        playerPoints = PlayerPoints.class.cast(playerPointsPlugin);
        return playerPoints != null;
    }
}
