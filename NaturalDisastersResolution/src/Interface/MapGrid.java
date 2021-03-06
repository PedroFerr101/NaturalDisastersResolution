import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapGrid {


    JPanel panel;
    GridCell[][] grid;
    Mapa mapa;
    Map<String, Posicao> posicaoAgentes;

    MapGrid(Mapa mapa){
        this.mapa = mapa;
        this.panel = new JPanel();
        this.panel.setLayout(new GridLayout(this.mapa.size, this.mapa.size));
        this.panel.setBounds(GUIConfig.MAP_GRID_X_POS, GUIConfig.MAP_GRID_Y_POS, GUIConfig.MAP_GRID_WIDTH, GUIConfig.MAP_GRID_HEIGHT);
        this.grid = new GridCell[this.mapa.size][this.mapa.size];
        this.posicaoAgentes = new HashMap<>();

        initializeMapGrid();
    }

    private void initializeMapGrid(){
        for (int i = 0; i < this.mapa.size; i++){
            for (int j = 0; j < this.mapa.size; j++){
                grid[i][j] = new GridCell(this.mapa.size);
                grid[i][j].p = new Posicao(i,j);
                panel.add(grid[i][j].gridCell);
            }
        }

        drawMapObjects(GridCell.HOUSE, mapa.habitacoes);
        drawMapObjects(GridCell.FOREST, mapa.floresta);
        drawMapObjects(GridCell.FUEL_STATION, mapa.getAllPostosCombustiveisAtivos());
        drawMapObjects(GridCell.WATER_SOURCE, mapa.postosAgua);
    }

    void changeMap(Mapa mapa){
        boolean resize = this.mapa.size != mapa.size;
        this.mapa = mapa;

        if(resize){
            this.panel.setLayout(new GridLayout(this.mapa.size, this.mapa.size));
            this.grid = new GridCell[this.mapa.size][this.mapa.size];
            this.panel.removeAll();
            initializeMapGrid();
            this.panel.revalidate();
            this.panel.repaint();
        }
        else{
            for (int i = 0; i < this.mapa.size; i++) {
                for (int j = 0; j < this.mapa.size; j++) {
                    grid[i][j].restoreCell();
                }
            }

            drawMapObjects(GridCell.HOUSE, mapa.habitacoes);
            drawMapObjects(GridCell.FOREST, mapa.floresta);
            drawMapObjects(GridCell.FUEL_STATION, mapa.getAllPostosCombustiveisAtivos());
            drawMapObjects(GridCell.WATER_SOURCE, mapa.postosAgua);
        }
    }

    private void drawMapObjects(int objectType, List<Posicao> objectPositions){
        for(Posicao p : objectPositions){
            GridCell gridCell =  this.grid[(int)p.pos_x][(int)p.pos_y];
            gridCell.setText("");
            gridCell.setType(objectType);
            gridCell.setImage();
        }
    }


    public void updateGridStatus(DeltaSimulationStatus stats) {
        List<GridCell> posicoesModificadas = new ArrayList<>();

        for(Posicao p : stats.novosIncendios){
            GridCell gridCell =  this.grid[(int)p.pos_x][(int)p.pos_y];
            gridCell.setOnFireState();
            posicoesModificadas.add(gridCell);
        }

        for(Posicao p : stats.celulasArdidas){
            GridCell gridCell =  this.grid[(int)p.pos_x][(int)p.pos_y];
            gridCell.setBurntState();
            posicoesModificadas.add(gridCell);
        }

        for(Posicao p : stats.celulasApagadas){
            GridCell gridCell =  this.grid[(int)p.pos_x][(int)p.pos_y];
            gridCell.setNormalState();
            posicoesModificadas.add(gridCell);
        }

        for(AgentStatus  as : stats.estadoAgentes){
            Posicao ultimaPosicao = this.posicaoAgentes.get(as.aid.getLocalName());

            if(ultimaPosicao != null){
                GridCell previousGridCell =  this.grid[(int)ultimaPosicao.pos_x][(int)ultimaPosicao.pos_y];
                previousGridCell.removeAgent(as.aid.getLocalName(), as.tipo);
                previousGridCell.setImage();
            }

            GridCell gridCell =  this.grid[(int)as.posAtual.pos_x][(int)as.posAtual.pos_y];
            gridCell.addAgent(as.aid.getLocalName(), as.tipo);
            this.posicaoAgentes.put(as.aid.getLocalName(), as.posAtual);
            posicoesModificadas.add(gridCell);
        }

        for(GridCell g : posicoesModificadas){
            g.setImage();
        }
    }

    void resetMapStatus(){
        for (int i = 0; i < this.mapa.size; i++) {
            for (int j = 0; j < this.mapa.size; j++) {
                grid[i][j].restoreCellStatus();
            }
        }
    }

    void removeVehicles() {
        for (int i = 0; i < this.mapa.size; i++) {
            for (int j = 0; j < this.mapa.size; j++) {
                grid[i][j].removeVehicles();
            }
        }
    }
}
