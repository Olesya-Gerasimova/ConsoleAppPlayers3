package ru.inno.course.player.service;

import ru.inno.course.player.data.DataProvider;
import ru.inno.course.player.data.DataProviderJSON;
import ru.inno.course.player.model.Player;

import java.util.*;

public class PlayerServiceImpl implements PlayerService {
    private Map<Integer, Player> players;
    private Set<String> nicknames;
    private int counter = 0;
    private final DataProvider provider;
    public PlayerServiceImpl() {
        provider = new DataProviderJSON();
        initStorages();
    }

    @Override
    public Player getPlayerById(int id) {
        if ( !this.players.containsKey(id)){
            throw new NoSuchElementException("No such user: " + id);
        }

        return this.players.get(id);
    }

    @Override
    public Collection<Player> getPlayers() {
        return this.players.values();
    }

    @Override
    public int createPlayer(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("Nickname cannot be empty or null.");
        }
        if (nicknames.contains(nickname)) {
            throw new IllegalArgumentException("Nickname is already in use: " + nickname);
        }
        counter++;
        Player player = new Player(counter, nickname, 0, true);
        this.players.put(player.getId(), player);
        this.nicknames.add(nickname);
        saveToFile();
        return player.getId();
    }

    @Override
    public Player deletePlayer(int id) {
        if ( !this.players.containsKey(id)){
            throw new NoSuchElementException("No such user: " + id);
        }

        Player p = this.players.remove(id);
        saveToFile();
        return p;
    }

    @Override
    public int addPoints(int playerId,  int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative.");
        }
        if (playerId <= 0 || !this.players.containsKey(playerId)) {
            throw new IllegalArgumentException("Invalid player ID: " + playerId);
        }

        Player player = this.players.get(playerId);
        int currentPoints = player.getPoints();
        int newPoints = currentPoints + points;
        player.setPoints(newPoints);
        saveToFile();
        return player.getPoints();
    }


    private void initStorages() {
        Collection<Player> currentList = Collections.EMPTY_LIST;
        try {
             currentList = provider.load();
        } catch (Exception ex){
            System.err.println("File loading error. "+ ex);
        }

        players = new HashMap<>();
        nicknames = new HashSet<>();

        for (Player player : currentList) {
            players.put(player.getId(), player);
            nicknames.add(player.getNick());
            if (player.getId() > counter) {
                counter = player.getId(); // Установка счетчика в максимальный ID
            }
        }
    }

    private void saveToFile() {
        try {
            this.provider.save(players.values());
        } catch (Exception ex){
            System.err.println("File saving error");
        }
    }

    public void clearPlayers() {
        players.clear();
        nicknames.clear();
        counter = 0;
    }

    public void updatePlayerNickname(int playerId, String newNickname) {
        if (nicknames.contains(newNickname)) {
            throw new IllegalArgumentException("Nickname is already in use: " + newNickname);
        }
        Player player = getPlayerById(playerId);
        nicknames.remove(player.getNick()); // Удаляем старый никнейм из набора
        player.setNick(newNickname);
        nicknames.add(newNickname); // Добавляем новый никнейм в набор
        saveToFile();
    }

}
