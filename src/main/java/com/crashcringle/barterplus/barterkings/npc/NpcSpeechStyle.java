package com.crashcringle.barterplus.barterkings.npc;

public enum NpcSpeechStyle {
    MILDLY_MEDIEVAL {
        @Override
        public String promptDescription() {
            return "Speak in a mildly medieval style";
        }
    },
    CRYPTIC {
        @Override
        public String promptDescription() {
            return "Speak in a cryptic and metaphorical style";
        }
    },
    SOUTHERN {
        @Override
        public String promptDescription() {
            return "Speak as though you are from the deep south";
        }
    },
    FORMAL {
        @Override
        public String promptDescription() {
            return "Speak in a formal and proper manner";
        }
    },
    INFORMAL {
        @Override
        public String promptDescription() {
            return "Speak in an informal and casual manner";
        }
    },
    STRAIGHTFORWARD {
        @Override
        public String promptDescription() {
            return "Speak in a straightforward manner";
        }
    },
    SLANG {
        @Override
        public String promptDescription() {
            return "Speak using slang and colloquialisms";
        }
    },
    PIRATE {
        @Override
        public String promptDescription() {
            return "Speak like a pirate";
        }
    };

    public abstract String promptDescription();
}
