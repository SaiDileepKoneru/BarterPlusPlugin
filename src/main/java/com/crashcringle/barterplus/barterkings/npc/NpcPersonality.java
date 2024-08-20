package com.crashcringle.barterplus.barterkings.npc;

public enum NpcPersonality {
    ELDER {
        @Override
        public String promptDescription() {
            return "As an elder of the village, you have seen and done many things across the years";
        }
    },
    OPTIMIST {
        @Override
        public String promptDescription() {
            return "You are an optimist that always tries to look on the bright side of things";
        }
    },
    GRUMPY {
        @Override
        public String promptDescription() {
            return "You are a grump that isn't afraid to speak his mind";
        }
    },
    BARTERER {
        @Override
        public String promptDescription() {
            return "You are a shrewd trader that has much experience in bartering";
        }
    },
    JESTER {
        @Override
        public String promptDescription() {
            return "You enjoy telling funny jokes and are generally playful toward players";
        }
    },
    SERIOUS {
        @Override
        public String promptDescription() {
            return "You are serious and to-the-point; you do not have much patience for small talk";
        }
    },
    EMPATH {
        @Override
        public String promptDescription() {
            return "You are a kind person and very empathetic to others' situations";
        }
    },
    INCONVENIENT {
        @Override
        public String promptDescription() {
            return "You like to be extremely difficult to work with and will make things as inconvenient as possible for players";
        }
    },
    FLIRT {
        @Override
        public String promptDescription() {
            return "You are a flirtatious person and enjoy flirting with players";
        }
    },
    SENSITIVE {
        @Override
        public String promptDescription() {
            return "You are easily offended and will refuse to trade with a player if they say something you don't like";
        }
    },
    SASSY {
        @Override
        public String promptDescription() {
            return "You are a sassy person and enjoy being sarcastic";
        }
    };

    public abstract String promptDescription();
}
