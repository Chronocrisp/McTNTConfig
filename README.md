commands:  
 setDisableTNT:
   False enables TNT, true disables TNT  
   permission: op  
   usage: "/setDisableTNT <false/true>"  
   aliases: ["dtnt"]  

  setTNTExplosionRadius:
    Sets explosion radius  
    permission: op  
    usage: "/setTNTExplosionRadius <float>"  
    aliases: ["tntradius"]  

  setTNTFuseTicks:
    Sets the ticks needed for TNT to explode  
    permission: op  
    usage: "/setDisableTNT <false/true>"  
    aliases: ["tntticks"]  

  tntConfigs:
    Displays current TNT properties such as fusetick etc  
    permission: op  
    usage: "/tntConfigs"  

  recordTNTDamage:
    Records any destruction by TNT  
    permission: op  
    usage: "/recordTNTDamage <true/false>"  

  restoreDefaultTNTConfig:
    Restores the minecraft default configurations for TNT  
    permission: op  
    usage: "/restoreDefaultTNTConfig"  

  clearBlockFile:
    Clears the file that saves the damages that TNT caused  
    permission: op  

  restoreTNTDamage:
    Restores any damages left by TNT  
    permission: op  
    usage: "/restoreTNTDamage <name of world>"  
