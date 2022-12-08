package skademaskinen;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import skademaskinen.Utils.Config;
import skademaskinen.Utils.Loggable;
import skademaskinen.Utils.Shell;
import skademaskinen.WorldOfWarcraft.BattleNetAPI;
import skademaskinen.WorldOfWarcraft.PvpTeam;
import skademaskinen.WorldOfWarcraft.RaidTeam;
import skademaskinen.Listeners.AutoCompleteListener;
import skademaskinen.Listeners.ButtonListener;
import skademaskinen.Listeners.LoggingListener;
import skademaskinen.Listeners.ModalListener;
import skademaskinen.Listeners.SlashCommandListener;

/**
 * The main class of The Nut Bot
 * It handles class abstractions and handles the main api, it also handles initialization.
 */
public class Bot implements Loggable{
    private static Config config;
    private static JDA jda;
    private static Shell shell;
    private static List<CommandData> commands;
    /**
     * The main method of the software, this method initializes everything and runs it.
     * @param args command line arguments that are passed after compilation, args[0] is always the access token for blizzard servers
     */
    public static void main(String[] args) {
        String accessToken = new JSONObject(args[0]).getString("access_token");
        new Bot(accessToken);
    }

    private static List<CommandData> generateCommands() {
        File[] files = new File("nutbot/src/main/java/skademaskinen/Commands").listFiles();
        List<CommandData> result = new ArrayList<>();
        for(File file : files){
            if(file.getName().equals("Command.java")) continue;
            try {
                Class<?> commandClass = Class.forName("skademaskinen.Commands."+file.getName().replace(".java", ""));
                //Shell.println("Initializing command: "+ commandClass.getSimpleName());
                Method method = commandClass.getMethod("configure");
                result.add((CommandData) method.invoke(commandClass));
            } 
            catch (Exception e) {
                Shell.exceptionHandler(e);
                if(e.getClass().equals(InvocationTargetException.class)){
                    Shell.exceptionHandler(((InvocationTargetException)e).getTargetException());
                }
            }
        }
        return result;
    }

    /**
     * The constructor, it is used to ensure that the main method never just throws exceptions but logs them instead
     * @param token The access token for the blizzard servers
     */
    public Bot(String token){
        try{
            config = new Config();
            jda = JDABuilder.createDefault(config.get("token"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .build();
            jda.addEventListener(new SlashCommandListener());
            jda.addEventListener(new ModalListener());
            jda.addEventListener(new ButtonListener());
            jda.addEventListener(new AutoCompleteListener());
            jda.addEventListener(new LoggingListener());
            shell = new Shell();
            BattleNetAPI.init(token);
            jda.awaitReady();
            commands = generateCommands();
            jda.updateCommands().addCommands(commands).queue();
            RaidTeam.update();
            PvpTeam.update();
            //exceptionTester();
            //jda.getGuildById("692410386657574952").getTextChannelById("1046840206562709514").sendMessageEmbeds(new EmbedBuilder().setTitle("init").build()).queue();
            new Thread(shell).start();
            log(true, new String[]{});
        }
        catch(Exception e){
            log(false, new String[]{e.getMessage()});
            Shell.exceptionHandler(e);

        }
    }

    /**
     * Getter for the api object of the Discord Java API
     * @return the initialized jda object of type JDA
     */
    public static JDA getJda() {
        return jda;
    }

    /**
     * Getter for the configuration object, this is used to ensure initialization
     * @return The config object of type Config
     */
    public static Config getConfig(){
        return config;
    }

    /**
     * The getter for the shell object, this is used to ensure initialization
     * @return The shell object of type Shell
     */
    public static Shell getShell(){
        return shell;
    }

    /**
     * This method is used to ensure consistency in replies to events across the interaction events for the bot, they always call this method at the end
     * @param hook The InteractionHook object, this is a callback that we can use to edit already sent acknowledgements to interactions
     * @param replyContent The content to add to this reply, it can have type MessageEmbed or String
     * @param actionRows If there is any actionRows this variable can be used, or null if there are no actionrows.
     */
    public static void replyToEvent(InteractionHook hook, Object replyContent, List<ActionRow> actionRows) {
        Class<?> ContentClass = replyContent.getClass();
        WebhookMessageEditAction<Message> action;
        if(ContentClass.equals(String.class)){
            action = hook.editOriginal((String) replyContent);
        }
        else if(ContentClass.equals(MessageEmbed.class)){
            action = hook.editOriginalEmbeds((MessageEmbed) replyContent);
        }
        else{
            action = hook.editOriginal("Error invalid reply class identified by: "+replyContent.getClass().getName());
        }
        if(actionRows != null){
            action.setComponents(actionRows);
        }
        action.queue();
    }
    public static void exceptionTester(){
        try {
            throw new Exception();
        } catch (Exception e) {
            Shell.exceptionHandler(e);
        }
    }
}
