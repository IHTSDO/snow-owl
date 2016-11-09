package com.b2international.snowowl.snomed.ecl.parser.antlr.internal; 

import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import com.b2international.snowowl.snomed.ecl.services.EclGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalEclParser extends AbstractInternalAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_OR", "RULE_AND", "RULE_COMMA", "RULE_MINUS", "RULE_COLON", "RULE_LT_EM", "RULE_LT", "RULE_DBL_LT", "RULE_GT_EM", "RULE_GT", "RULE_DBL_GT", "RULE_CARET", "RULE_PIPE", "RULE_WILDCARD", "RULE_REVERSED", "RULE_EQUAL", "RULE_NOT_EQUAL", "RULE_ROUND_OPEN", "RULE_ROUND_CLOSE", "RULE_DIGIT_NONZERO", "RULE_ZERO", "RULE_WS", "RULE_NOT", "RULE_LETTER", "RULE_PLUS", "RULE_CURLY_OPEN", "RULE_CURLY_CLOSE", "RULE_SQUARE_OPEN", "RULE_SQUARE_CLOSE", "RULE_DOT", "RULE_OTHER_CHARACTER", "RULE_ML_COMMENT", "RULE_SL_COMMENT"
    };
    public static final int RULE_DIGIT_NONZERO=23;
    public static final int RULE_CURLY_OPEN=29;
    public static final int RULE_DBL_GT=14;
    public static final int RULE_ROUND_CLOSE=22;
    public static final int RULE_GT=13;
    public static final int RULE_NOT=26;
    public static final int RULE_AND=5;
    public static final int RULE_REVERSED=18;
    public static final int RULE_SL_COMMENT=36;
    public static final int RULE_ROUND_OPEN=21;
    public static final int RULE_OTHER_CHARACTER=34;
    public static final int RULE_DBL_LT=11;
    public static final int RULE_PLUS=28;
    public static final int RULE_NOT_EQUAL=20;
    public static final int RULE_OR=4;
    public static final int RULE_DOT=33;
    public static final int EOF=-1;
    public static final int RULE_SQUARE_CLOSE=32;
    public static final int RULE_EQUAL=19;
    public static final int RULE_SQUARE_OPEN=31;
    public static final int RULE_COMMA=6;
    public static final int RULE_LT_EM=9;
    public static final int RULE_GT_EM=12;
    public static final int RULE_WS=25;
    public static final int RULE_CURLY_CLOSE=30;
    public static final int RULE_ZERO=24;
    public static final int RULE_COLON=8;
    public static final int RULE_MINUS=7;
    public static final int RULE_LETTER=27;
    public static final int RULE_LT=10;
    public static final int RULE_CARET=15;
    public static final int RULE_PIPE=16;
    public static final int RULE_ML_COMMENT=35;
    public static final int RULE_WILDCARD=17;

    // delegates
    // delegators


        public InternalEclParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public InternalEclParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return InternalEclParser.tokenNames; }
    public String getGrammarFileName() { return "../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g"; }



     	private EclGrammarAccess grammarAccess;
     	
        public InternalEclParser(TokenStream input, EclGrammarAccess grammarAccess) {
            this(input);
            this.grammarAccess = grammarAccess;
            registerRules(grammarAccess.getGrammar());
        }
        
        @Override
        protected String getFirstRuleName() {
        	return "ExpressionConstraint";	
       	}
       	
       	@Override
       	protected EclGrammarAccess getGrammarAccess() {
       		return grammarAccess;
       	}



    // $ANTLR start "entryRuleExpressionConstraint"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:67:1: entryRuleExpressionConstraint returns [EObject current=null] : iv_ruleExpressionConstraint= ruleExpressionConstraint EOF ;
    public final EObject entryRuleExpressionConstraint() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExpressionConstraint = null;


         
        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens("RULE_WS", "RULE_SL_COMMENT", "RULE_ML_COMMENT");
        	
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:71:2: (iv_ruleExpressionConstraint= ruleExpressionConstraint EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:72:2: iv_ruleExpressionConstraint= ruleExpressionConstraint EOF
            {
             newCompositeNode(grammarAccess.getExpressionConstraintRule()); 
            pushFollow(FOLLOW_ruleExpressionConstraint_in_entryRuleExpressionConstraint81);
            iv_ruleExpressionConstraint=ruleExpressionConstraint();

            state._fsp--;

             current =iv_ruleExpressionConstraint; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExpressionConstraint91); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "entryRuleExpressionConstraint"


    // $ANTLR start "ruleExpressionConstraint"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:82:1: ruleExpressionConstraint returns [EObject current=null] : this_OrExpressionConstraint_0= ruleOrExpressionConstraint ;
    public final EObject ruleExpressionConstraint() throws RecognitionException {
        EObject current = null;

        EObject this_OrExpressionConstraint_0 = null;


         enterRule(); 
        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens("RULE_WS", "RULE_SL_COMMENT", "RULE_ML_COMMENT");
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:86:28: (this_OrExpressionConstraint_0= ruleOrExpressionConstraint )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:88:5: this_OrExpressionConstraint_0= ruleOrExpressionConstraint
            {
             
                    newCompositeNode(grammarAccess.getExpressionConstraintAccess().getOrExpressionConstraintParserRuleCall()); 
                
            pushFollow(FOLLOW_ruleOrExpressionConstraint_in_ruleExpressionConstraint141);
            this_OrExpressionConstraint_0=ruleOrExpressionConstraint();

            state._fsp--;

             
                    current = this_OrExpressionConstraint_0; 
                    afterParserOrEnumRuleCall();
                

            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "ruleExpressionConstraint"


    // $ANTLR start "entryRuleOrExpressionConstraint"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:107:1: entryRuleOrExpressionConstraint returns [EObject current=null] : iv_ruleOrExpressionConstraint= ruleOrExpressionConstraint EOF ;
    public final EObject entryRuleOrExpressionConstraint() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOrExpressionConstraint = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:108:2: (iv_ruleOrExpressionConstraint= ruleOrExpressionConstraint EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:109:2: iv_ruleOrExpressionConstraint= ruleOrExpressionConstraint EOF
            {
             newCompositeNode(grammarAccess.getOrExpressionConstraintRule()); 
            pushFollow(FOLLOW_ruleOrExpressionConstraint_in_entryRuleOrExpressionConstraint179);
            iv_ruleOrExpressionConstraint=ruleOrExpressionConstraint();

            state._fsp--;

             current =iv_ruleOrExpressionConstraint; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleOrExpressionConstraint189); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOrExpressionConstraint"


    // $ANTLR start "ruleOrExpressionConstraint"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:116:1: ruleOrExpressionConstraint returns [EObject current=null] : (this_AndExpressionConstraint_0= ruleAndExpressionConstraint ( () this_OR_2= RULE_OR ( (lv_right_3_0= ruleAndExpressionConstraint ) ) )* ) ;
    public final EObject ruleOrExpressionConstraint() throws RecognitionException {
        EObject current = null;

        Token this_OR_2=null;
        EObject this_AndExpressionConstraint_0 = null;

        EObject lv_right_3_0 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:119:28: ( (this_AndExpressionConstraint_0= ruleAndExpressionConstraint ( () this_OR_2= RULE_OR ( (lv_right_3_0= ruleAndExpressionConstraint ) ) )* ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:120:1: (this_AndExpressionConstraint_0= ruleAndExpressionConstraint ( () this_OR_2= RULE_OR ( (lv_right_3_0= ruleAndExpressionConstraint ) ) )* )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:120:1: (this_AndExpressionConstraint_0= ruleAndExpressionConstraint ( () this_OR_2= RULE_OR ( (lv_right_3_0= ruleAndExpressionConstraint ) ) )* )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:121:5: this_AndExpressionConstraint_0= ruleAndExpressionConstraint ( () this_OR_2= RULE_OR ( (lv_right_3_0= ruleAndExpressionConstraint ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getOrExpressionConstraintAccess().getAndExpressionConstraintParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleAndExpressionConstraint_in_ruleOrExpressionConstraint236);
            this_AndExpressionConstraint_0=ruleAndExpressionConstraint();

            state._fsp--;

             
                    current = this_AndExpressionConstraint_0; 
                    afterParserOrEnumRuleCall();
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:129:1: ( () this_OR_2= RULE_OR ( (lv_right_3_0= ruleAndExpressionConstraint ) ) )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==RULE_OR) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:129:2: () this_OR_2= RULE_OR ( (lv_right_3_0= ruleAndExpressionConstraint ) )
            	    {
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:129:2: ()
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:130:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getOrExpressionConstraintAccess().getOrExpressionConstraintLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    this_OR_2=(Token)match(input,RULE_OR,FOLLOW_RULE_OR_in_ruleOrExpressionConstraint256); 
            	     
            	        newLeafNode(this_OR_2, grammarAccess.getOrExpressionConstraintAccess().getORTerminalRuleCall_1_1()); 
            	        
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:139:1: ( (lv_right_3_0= ruleAndExpressionConstraint ) )
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:140:1: (lv_right_3_0= ruleAndExpressionConstraint )
            	    {
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:140:1: (lv_right_3_0= ruleAndExpressionConstraint )
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:141:3: lv_right_3_0= ruleAndExpressionConstraint
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getOrExpressionConstraintAccess().getRightAndExpressionConstraintParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleAndExpressionConstraint_in_ruleOrExpressionConstraint276);
            	    lv_right_3_0=ruleAndExpressionConstraint();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getOrExpressionConstraintRule());
            	    	        }
            	           		set(
            	           			current, 
            	           			"right",
            	            		lv_right_3_0, 
            	            		"AndExpressionConstraint");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOrExpressionConstraint"


    // $ANTLR start "entryRuleAndExpressionConstraint"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:165:1: entryRuleAndExpressionConstraint returns [EObject current=null] : iv_ruleAndExpressionConstraint= ruleAndExpressionConstraint EOF ;
    public final EObject entryRuleAndExpressionConstraint() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAndExpressionConstraint = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:166:2: (iv_ruleAndExpressionConstraint= ruleAndExpressionConstraint EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:167:2: iv_ruleAndExpressionConstraint= ruleAndExpressionConstraint EOF
            {
             newCompositeNode(grammarAccess.getAndExpressionConstraintRule()); 
            pushFollow(FOLLOW_ruleAndExpressionConstraint_in_entryRuleAndExpressionConstraint314);
            iv_ruleAndExpressionConstraint=ruleAndExpressionConstraint();

            state._fsp--;

             current =iv_ruleAndExpressionConstraint; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleAndExpressionConstraint324); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAndExpressionConstraint"


    // $ANTLR start "ruleAndExpressionConstraint"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:174:1: ruleAndExpressionConstraint returns [EObject current=null] : (this_ExclusionExpressionConstraint_0= ruleExclusionExpressionConstraint ( () (this_AND_2= RULE_AND | this_COMMA_3= RULE_COMMA ) ( (lv_right_4_0= ruleExclusionExpressionConstraint ) ) )* ) ;
    public final EObject ruleAndExpressionConstraint() throws RecognitionException {
        EObject current = null;

        Token this_AND_2=null;
        Token this_COMMA_3=null;
        EObject this_ExclusionExpressionConstraint_0 = null;

        EObject lv_right_4_0 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:177:28: ( (this_ExclusionExpressionConstraint_0= ruleExclusionExpressionConstraint ( () (this_AND_2= RULE_AND | this_COMMA_3= RULE_COMMA ) ( (lv_right_4_0= ruleExclusionExpressionConstraint ) ) )* ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:178:1: (this_ExclusionExpressionConstraint_0= ruleExclusionExpressionConstraint ( () (this_AND_2= RULE_AND | this_COMMA_3= RULE_COMMA ) ( (lv_right_4_0= ruleExclusionExpressionConstraint ) ) )* )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:178:1: (this_ExclusionExpressionConstraint_0= ruleExclusionExpressionConstraint ( () (this_AND_2= RULE_AND | this_COMMA_3= RULE_COMMA ) ( (lv_right_4_0= ruleExclusionExpressionConstraint ) ) )* )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:179:5: this_ExclusionExpressionConstraint_0= ruleExclusionExpressionConstraint ( () (this_AND_2= RULE_AND | this_COMMA_3= RULE_COMMA ) ( (lv_right_4_0= ruleExclusionExpressionConstraint ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getAndExpressionConstraintAccess().getExclusionExpressionConstraintParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleExclusionExpressionConstraint_in_ruleAndExpressionConstraint371);
            this_ExclusionExpressionConstraint_0=ruleExclusionExpressionConstraint();

            state._fsp--;

             
                    current = this_ExclusionExpressionConstraint_0; 
                    afterParserOrEnumRuleCall();
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:187:1: ( () (this_AND_2= RULE_AND | this_COMMA_3= RULE_COMMA ) ( (lv_right_4_0= ruleExclusionExpressionConstraint ) ) )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>=RULE_AND && LA3_0<=RULE_COMMA)) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:187:2: () (this_AND_2= RULE_AND | this_COMMA_3= RULE_COMMA ) ( (lv_right_4_0= ruleExclusionExpressionConstraint ) )
            	    {
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:187:2: ()
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:188:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getAndExpressionConstraintAccess().getAndExpressionConstraintLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:193:2: (this_AND_2= RULE_AND | this_COMMA_3= RULE_COMMA )
            	    int alt2=2;
            	    int LA2_0 = input.LA(1);

            	    if ( (LA2_0==RULE_AND) ) {
            	        alt2=1;
            	    }
            	    else if ( (LA2_0==RULE_COMMA) ) {
            	        alt2=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 2, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt2) {
            	        case 1 :
            	            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:193:3: this_AND_2= RULE_AND
            	            {
            	            this_AND_2=(Token)match(input,RULE_AND,FOLLOW_RULE_AND_in_ruleAndExpressionConstraint392); 
            	             
            	                newLeafNode(this_AND_2, grammarAccess.getAndExpressionConstraintAccess().getANDTerminalRuleCall_1_1_0()); 
            	                

            	            }
            	            break;
            	        case 2 :
            	            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:198:6: this_COMMA_3= RULE_COMMA
            	            {
            	            this_COMMA_3=(Token)match(input,RULE_COMMA,FOLLOW_RULE_COMMA_in_ruleAndExpressionConstraint408); 
            	             
            	                newLeafNode(this_COMMA_3, grammarAccess.getAndExpressionConstraintAccess().getCOMMATerminalRuleCall_1_1_1()); 
            	                

            	            }
            	            break;

            	    }

            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:202:2: ( (lv_right_4_0= ruleExclusionExpressionConstraint ) )
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:203:1: (lv_right_4_0= ruleExclusionExpressionConstraint )
            	    {
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:203:1: (lv_right_4_0= ruleExclusionExpressionConstraint )
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:204:3: lv_right_4_0= ruleExclusionExpressionConstraint
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getAndExpressionConstraintAccess().getRightExclusionExpressionConstraintParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExclusionExpressionConstraint_in_ruleAndExpressionConstraint429);
            	    lv_right_4_0=ruleExclusionExpressionConstraint();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getAndExpressionConstraintRule());
            	    	        }
            	           		set(
            	           			current, 
            	           			"right",
            	            		lv_right_4_0, 
            	            		"ExclusionExpressionConstraint");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAndExpressionConstraint"


    // $ANTLR start "entryRuleExclusionExpressionConstraint"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:228:1: entryRuleExclusionExpressionConstraint returns [EObject current=null] : iv_ruleExclusionExpressionConstraint= ruleExclusionExpressionConstraint EOF ;
    public final EObject entryRuleExclusionExpressionConstraint() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExclusionExpressionConstraint = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:229:2: (iv_ruleExclusionExpressionConstraint= ruleExclusionExpressionConstraint EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:230:2: iv_ruleExclusionExpressionConstraint= ruleExclusionExpressionConstraint EOF
            {
             newCompositeNode(grammarAccess.getExclusionExpressionConstraintRule()); 
            pushFollow(FOLLOW_ruleExclusionExpressionConstraint_in_entryRuleExclusionExpressionConstraint467);
            iv_ruleExclusionExpressionConstraint=ruleExclusionExpressionConstraint();

            state._fsp--;

             current =iv_ruleExclusionExpressionConstraint; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExclusionExpressionConstraint477); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExclusionExpressionConstraint"


    // $ANTLR start "ruleExclusionExpressionConstraint"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:237:1: ruleExclusionExpressionConstraint returns [EObject current=null] : (this_RefinedExpressionConstraint_0= ruleRefinedExpressionConstraint ( () this_MINUS_2= RULE_MINUS ( (lv_right_3_0= ruleRefinedExpressionConstraint ) ) )? ) ;
    public final EObject ruleExclusionExpressionConstraint() throws RecognitionException {
        EObject current = null;

        Token this_MINUS_2=null;
        EObject this_RefinedExpressionConstraint_0 = null;

        EObject lv_right_3_0 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:240:28: ( (this_RefinedExpressionConstraint_0= ruleRefinedExpressionConstraint ( () this_MINUS_2= RULE_MINUS ( (lv_right_3_0= ruleRefinedExpressionConstraint ) ) )? ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:241:1: (this_RefinedExpressionConstraint_0= ruleRefinedExpressionConstraint ( () this_MINUS_2= RULE_MINUS ( (lv_right_3_0= ruleRefinedExpressionConstraint ) ) )? )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:241:1: (this_RefinedExpressionConstraint_0= ruleRefinedExpressionConstraint ( () this_MINUS_2= RULE_MINUS ( (lv_right_3_0= ruleRefinedExpressionConstraint ) ) )? )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:242:5: this_RefinedExpressionConstraint_0= ruleRefinedExpressionConstraint ( () this_MINUS_2= RULE_MINUS ( (lv_right_3_0= ruleRefinedExpressionConstraint ) ) )?
            {
             
                    newCompositeNode(grammarAccess.getExclusionExpressionConstraintAccess().getRefinedExpressionConstraintParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleRefinedExpressionConstraint_in_ruleExclusionExpressionConstraint524);
            this_RefinedExpressionConstraint_0=ruleRefinedExpressionConstraint();

            state._fsp--;

             
                    current = this_RefinedExpressionConstraint_0; 
                    afterParserOrEnumRuleCall();
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:250:1: ( () this_MINUS_2= RULE_MINUS ( (lv_right_3_0= ruleRefinedExpressionConstraint ) ) )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==RULE_MINUS) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:250:2: () this_MINUS_2= RULE_MINUS ( (lv_right_3_0= ruleRefinedExpressionConstraint ) )
                    {
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:250:2: ()
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:251:5: 
                    {

                            current = forceCreateModelElementAndSet(
                                grammarAccess.getExclusionExpressionConstraintAccess().getExclusionExpressionConstraintLeftAction_1_0(),
                                current);
                        

                    }

                    this_MINUS_2=(Token)match(input,RULE_MINUS,FOLLOW_RULE_MINUS_in_ruleExclusionExpressionConstraint544); 
                     
                        newLeafNode(this_MINUS_2, grammarAccess.getExclusionExpressionConstraintAccess().getMINUSTerminalRuleCall_1_1()); 
                        
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:260:1: ( (lv_right_3_0= ruleRefinedExpressionConstraint ) )
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:261:1: (lv_right_3_0= ruleRefinedExpressionConstraint )
                    {
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:261:1: (lv_right_3_0= ruleRefinedExpressionConstraint )
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:262:3: lv_right_3_0= ruleRefinedExpressionConstraint
                    {
                     
                    	        newCompositeNode(grammarAccess.getExclusionExpressionConstraintAccess().getRightRefinedExpressionConstraintParserRuleCall_1_2_0()); 
                    	    
                    pushFollow(FOLLOW_ruleRefinedExpressionConstraint_in_ruleExclusionExpressionConstraint564);
                    lv_right_3_0=ruleRefinedExpressionConstraint();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getExclusionExpressionConstraintRule());
                    	        }
                           		set(
                           			current, 
                           			"right",
                            		lv_right_3_0, 
                            		"RefinedExpressionConstraint");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }
                    break;

            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExclusionExpressionConstraint"


    // $ANTLR start "entryRuleRefinedExpressionConstraint"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:286:1: entryRuleRefinedExpressionConstraint returns [EObject current=null] : iv_ruleRefinedExpressionConstraint= ruleRefinedExpressionConstraint EOF ;
    public final EObject entryRuleRefinedExpressionConstraint() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRefinedExpressionConstraint = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:287:2: (iv_ruleRefinedExpressionConstraint= ruleRefinedExpressionConstraint EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:288:2: iv_ruleRefinedExpressionConstraint= ruleRefinedExpressionConstraint EOF
            {
             newCompositeNode(grammarAccess.getRefinedExpressionConstraintRule()); 
            pushFollow(FOLLOW_ruleRefinedExpressionConstraint_in_entryRuleRefinedExpressionConstraint602);
            iv_ruleRefinedExpressionConstraint=ruleRefinedExpressionConstraint();

            state._fsp--;

             current =iv_ruleRefinedExpressionConstraint; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleRefinedExpressionConstraint612); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRefinedExpressionConstraint"


    // $ANTLR start "ruleRefinedExpressionConstraint"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:295:1: ruleRefinedExpressionConstraint returns [EObject current=null] : (this_SimpleExpressionConstraint_0= ruleSimpleExpressionConstraint ( () this_COLON_2= RULE_COLON ( (lv_refinement_3_0= ruleRefinement ) ) )? ) ;
    public final EObject ruleRefinedExpressionConstraint() throws RecognitionException {
        EObject current = null;

        Token this_COLON_2=null;
        EObject this_SimpleExpressionConstraint_0 = null;

        EObject lv_refinement_3_0 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:298:28: ( (this_SimpleExpressionConstraint_0= ruleSimpleExpressionConstraint ( () this_COLON_2= RULE_COLON ( (lv_refinement_3_0= ruleRefinement ) ) )? ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:299:1: (this_SimpleExpressionConstraint_0= ruleSimpleExpressionConstraint ( () this_COLON_2= RULE_COLON ( (lv_refinement_3_0= ruleRefinement ) ) )? )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:299:1: (this_SimpleExpressionConstraint_0= ruleSimpleExpressionConstraint ( () this_COLON_2= RULE_COLON ( (lv_refinement_3_0= ruleRefinement ) ) )? )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:300:5: this_SimpleExpressionConstraint_0= ruleSimpleExpressionConstraint ( () this_COLON_2= RULE_COLON ( (lv_refinement_3_0= ruleRefinement ) ) )?
            {
             
                    newCompositeNode(grammarAccess.getRefinedExpressionConstraintAccess().getSimpleExpressionConstraintParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleSimpleExpressionConstraint_in_ruleRefinedExpressionConstraint659);
            this_SimpleExpressionConstraint_0=ruleSimpleExpressionConstraint();

            state._fsp--;

             
                    current = this_SimpleExpressionConstraint_0; 
                    afterParserOrEnumRuleCall();
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:308:1: ( () this_COLON_2= RULE_COLON ( (lv_refinement_3_0= ruleRefinement ) ) )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==RULE_COLON) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:308:2: () this_COLON_2= RULE_COLON ( (lv_refinement_3_0= ruleRefinement ) )
                    {
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:308:2: ()
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:309:5: 
                    {

                            current = forceCreateModelElementAndSet(
                                grammarAccess.getRefinedExpressionConstraintAccess().getRefinedExpressionConstraintConstraintAction_1_0(),
                                current);
                        

                    }

                    this_COLON_2=(Token)match(input,RULE_COLON,FOLLOW_RULE_COLON_in_ruleRefinedExpressionConstraint679); 
                     
                        newLeafNode(this_COLON_2, grammarAccess.getRefinedExpressionConstraintAccess().getCOLONTerminalRuleCall_1_1()); 
                        
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:318:1: ( (lv_refinement_3_0= ruleRefinement ) )
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:319:1: (lv_refinement_3_0= ruleRefinement )
                    {
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:319:1: (lv_refinement_3_0= ruleRefinement )
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:320:3: lv_refinement_3_0= ruleRefinement
                    {
                     
                    	        newCompositeNode(grammarAccess.getRefinedExpressionConstraintAccess().getRefinementRefinementParserRuleCall_1_2_0()); 
                    	    
                    pushFollow(FOLLOW_ruleRefinement_in_ruleRefinedExpressionConstraint699);
                    lv_refinement_3_0=ruleRefinement();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getRefinedExpressionConstraintRule());
                    	        }
                           		set(
                           			current, 
                           			"refinement",
                            		lv_refinement_3_0, 
                            		"Refinement");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }
                    break;

            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRefinedExpressionConstraint"


    // $ANTLR start "entryRuleSimpleExpressionConstraint"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:344:1: entryRuleSimpleExpressionConstraint returns [EObject current=null] : iv_ruleSimpleExpressionConstraint= ruleSimpleExpressionConstraint EOF ;
    public final EObject entryRuleSimpleExpressionConstraint() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSimpleExpressionConstraint = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:345:2: (iv_ruleSimpleExpressionConstraint= ruleSimpleExpressionConstraint EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:346:2: iv_ruleSimpleExpressionConstraint= ruleSimpleExpressionConstraint EOF
            {
             newCompositeNode(grammarAccess.getSimpleExpressionConstraintRule()); 
            pushFollow(FOLLOW_ruleSimpleExpressionConstraint_in_entryRuleSimpleExpressionConstraint737);
            iv_ruleSimpleExpressionConstraint=ruleSimpleExpressionConstraint();

            state._fsp--;

             current =iv_ruleSimpleExpressionConstraint; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleSimpleExpressionConstraint747); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSimpleExpressionConstraint"


    // $ANTLR start "ruleSimpleExpressionConstraint"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:353:1: ruleSimpleExpressionConstraint returns [EObject current=null] : (this_ChildOf_0= ruleChildOf | this_DescendantOf_1= ruleDescendantOf | this_DescendantOrSelfOf_2= ruleDescendantOrSelfOf | this_ParentOf_3= ruleParentOf | this_AncestorOf_4= ruleAncestorOf | this_AncestorOrSelfOf_5= ruleAncestorOrSelfOf | this_FocusConcept_6= ruleFocusConcept ) ;
    public final EObject ruleSimpleExpressionConstraint() throws RecognitionException {
        EObject current = null;

        EObject this_ChildOf_0 = null;

        EObject this_DescendantOf_1 = null;

        EObject this_DescendantOrSelfOf_2 = null;

        EObject this_ParentOf_3 = null;

        EObject this_AncestorOf_4 = null;

        EObject this_AncestorOrSelfOf_5 = null;

        EObject this_FocusConcept_6 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:356:28: ( (this_ChildOf_0= ruleChildOf | this_DescendantOf_1= ruleDescendantOf | this_DescendantOrSelfOf_2= ruleDescendantOrSelfOf | this_ParentOf_3= ruleParentOf | this_AncestorOf_4= ruleAncestorOf | this_AncestorOrSelfOf_5= ruleAncestorOrSelfOf | this_FocusConcept_6= ruleFocusConcept ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:357:1: (this_ChildOf_0= ruleChildOf | this_DescendantOf_1= ruleDescendantOf | this_DescendantOrSelfOf_2= ruleDescendantOrSelfOf | this_ParentOf_3= ruleParentOf | this_AncestorOf_4= ruleAncestorOf | this_AncestorOrSelfOf_5= ruleAncestorOrSelfOf | this_FocusConcept_6= ruleFocusConcept )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:357:1: (this_ChildOf_0= ruleChildOf | this_DescendantOf_1= ruleDescendantOf | this_DescendantOrSelfOf_2= ruleDescendantOrSelfOf | this_ParentOf_3= ruleParentOf | this_AncestorOf_4= ruleAncestorOf | this_AncestorOrSelfOf_5= ruleAncestorOrSelfOf | this_FocusConcept_6= ruleFocusConcept )
            int alt6=7;
            switch ( input.LA(1) ) {
            case RULE_LT_EM:
                {
                alt6=1;
                }
                break;
            case RULE_LT:
                {
                alt6=2;
                }
                break;
            case RULE_DBL_LT:
                {
                alt6=3;
                }
                break;
            case RULE_GT_EM:
                {
                alt6=4;
                }
                break;
            case RULE_GT:
                {
                alt6=5;
                }
                break;
            case RULE_DBL_GT:
                {
                alt6=6;
                }
                break;
            case RULE_CARET:
            case RULE_WILDCARD:
            case RULE_ROUND_OPEN:
            case RULE_DIGIT_NONZERO:
                {
                alt6=7;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }

            switch (alt6) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:358:5: this_ChildOf_0= ruleChildOf
                    {
                     
                            newCompositeNode(grammarAccess.getSimpleExpressionConstraintAccess().getChildOfParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleChildOf_in_ruleSimpleExpressionConstraint794);
                    this_ChildOf_0=ruleChildOf();

                    state._fsp--;

                     
                            current = this_ChildOf_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:368:5: this_DescendantOf_1= ruleDescendantOf
                    {
                     
                            newCompositeNode(grammarAccess.getSimpleExpressionConstraintAccess().getDescendantOfParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleDescendantOf_in_ruleSimpleExpressionConstraint821);
                    this_DescendantOf_1=ruleDescendantOf();

                    state._fsp--;

                     
                            current = this_DescendantOf_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 3 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:378:5: this_DescendantOrSelfOf_2= ruleDescendantOrSelfOf
                    {
                     
                            newCompositeNode(grammarAccess.getSimpleExpressionConstraintAccess().getDescendantOrSelfOfParserRuleCall_2()); 
                        
                    pushFollow(FOLLOW_ruleDescendantOrSelfOf_in_ruleSimpleExpressionConstraint848);
                    this_DescendantOrSelfOf_2=ruleDescendantOrSelfOf();

                    state._fsp--;

                     
                            current = this_DescendantOrSelfOf_2; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 4 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:388:5: this_ParentOf_3= ruleParentOf
                    {
                     
                            newCompositeNode(grammarAccess.getSimpleExpressionConstraintAccess().getParentOfParserRuleCall_3()); 
                        
                    pushFollow(FOLLOW_ruleParentOf_in_ruleSimpleExpressionConstraint875);
                    this_ParentOf_3=ruleParentOf();

                    state._fsp--;

                     
                            current = this_ParentOf_3; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 5 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:398:5: this_AncestorOf_4= ruleAncestorOf
                    {
                     
                            newCompositeNode(grammarAccess.getSimpleExpressionConstraintAccess().getAncestorOfParserRuleCall_4()); 
                        
                    pushFollow(FOLLOW_ruleAncestorOf_in_ruleSimpleExpressionConstraint902);
                    this_AncestorOf_4=ruleAncestorOf();

                    state._fsp--;

                     
                            current = this_AncestorOf_4; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 6 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:408:5: this_AncestorOrSelfOf_5= ruleAncestorOrSelfOf
                    {
                     
                            newCompositeNode(grammarAccess.getSimpleExpressionConstraintAccess().getAncestorOrSelfOfParserRuleCall_5()); 
                        
                    pushFollow(FOLLOW_ruleAncestorOrSelfOf_in_ruleSimpleExpressionConstraint929);
                    this_AncestorOrSelfOf_5=ruleAncestorOrSelfOf();

                    state._fsp--;

                     
                            current = this_AncestorOrSelfOf_5; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 7 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:418:5: this_FocusConcept_6= ruleFocusConcept
                    {
                     
                            newCompositeNode(grammarAccess.getSimpleExpressionConstraintAccess().getFocusConceptParserRuleCall_6()); 
                        
                    pushFollow(FOLLOW_ruleFocusConcept_in_ruleSimpleExpressionConstraint956);
                    this_FocusConcept_6=ruleFocusConcept();

                    state._fsp--;

                     
                            current = this_FocusConcept_6; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSimpleExpressionConstraint"


    // $ANTLR start "entryRuleFocusConcept"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:434:1: entryRuleFocusConcept returns [EObject current=null] : iv_ruleFocusConcept= ruleFocusConcept EOF ;
    public final EObject entryRuleFocusConcept() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFocusConcept = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:435:2: (iv_ruleFocusConcept= ruleFocusConcept EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:436:2: iv_ruleFocusConcept= ruleFocusConcept EOF
            {
             newCompositeNode(grammarAccess.getFocusConceptRule()); 
            pushFollow(FOLLOW_ruleFocusConcept_in_entryRuleFocusConcept991);
            iv_ruleFocusConcept=ruleFocusConcept();

            state._fsp--;

             current =iv_ruleFocusConcept; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleFocusConcept1001); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleFocusConcept"


    // $ANTLR start "ruleFocusConcept"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:443:1: ruleFocusConcept returns [EObject current=null] : (this_MemberOf_0= ruleMemberOf | this_ConceptReference_1= ruleConceptReference | this_Any_2= ruleAny | this_NestedExpression_3= ruleNestedExpression ) ;
    public final EObject ruleFocusConcept() throws RecognitionException {
        EObject current = null;

        EObject this_MemberOf_0 = null;

        EObject this_ConceptReference_1 = null;

        EObject this_Any_2 = null;

        EObject this_NestedExpression_3 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:446:28: ( (this_MemberOf_0= ruleMemberOf | this_ConceptReference_1= ruleConceptReference | this_Any_2= ruleAny | this_NestedExpression_3= ruleNestedExpression ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:447:1: (this_MemberOf_0= ruleMemberOf | this_ConceptReference_1= ruleConceptReference | this_Any_2= ruleAny | this_NestedExpression_3= ruleNestedExpression )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:447:1: (this_MemberOf_0= ruleMemberOf | this_ConceptReference_1= ruleConceptReference | this_Any_2= ruleAny | this_NestedExpression_3= ruleNestedExpression )
            int alt7=4;
            switch ( input.LA(1) ) {
            case RULE_CARET:
                {
                alt7=1;
                }
                break;
            case RULE_DIGIT_NONZERO:
                {
                alt7=2;
                }
                break;
            case RULE_WILDCARD:
                {
                alt7=3;
                }
                break;
            case RULE_ROUND_OPEN:
                {
                alt7=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }

            switch (alt7) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:448:5: this_MemberOf_0= ruleMemberOf
                    {
                     
                            newCompositeNode(grammarAccess.getFocusConceptAccess().getMemberOfParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleMemberOf_in_ruleFocusConcept1048);
                    this_MemberOf_0=ruleMemberOf();

                    state._fsp--;

                     
                            current = this_MemberOf_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:458:5: this_ConceptReference_1= ruleConceptReference
                    {
                     
                            newCompositeNode(grammarAccess.getFocusConceptAccess().getConceptReferenceParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleConceptReference_in_ruleFocusConcept1075);
                    this_ConceptReference_1=ruleConceptReference();

                    state._fsp--;

                     
                            current = this_ConceptReference_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 3 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:468:5: this_Any_2= ruleAny
                    {
                     
                            newCompositeNode(grammarAccess.getFocusConceptAccess().getAnyParserRuleCall_2()); 
                        
                    pushFollow(FOLLOW_ruleAny_in_ruleFocusConcept1102);
                    this_Any_2=ruleAny();

                    state._fsp--;

                     
                            current = this_Any_2; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 4 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:478:5: this_NestedExpression_3= ruleNestedExpression
                    {
                     
                            newCompositeNode(grammarAccess.getFocusConceptAccess().getNestedExpressionParserRuleCall_3()); 
                        
                    pushFollow(FOLLOW_ruleNestedExpression_in_ruleFocusConcept1129);
                    this_NestedExpression_3=ruleNestedExpression();

                    state._fsp--;

                     
                            current = this_NestedExpression_3; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleFocusConcept"


    // $ANTLR start "entryRuleChildOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:494:1: entryRuleChildOf returns [EObject current=null] : iv_ruleChildOf= ruleChildOf EOF ;
    public final EObject entryRuleChildOf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleChildOf = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:495:2: (iv_ruleChildOf= ruleChildOf EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:496:2: iv_ruleChildOf= ruleChildOf EOF
            {
             newCompositeNode(grammarAccess.getChildOfRule()); 
            pushFollow(FOLLOW_ruleChildOf_in_entryRuleChildOf1164);
            iv_ruleChildOf=ruleChildOf();

            state._fsp--;

             current =iv_ruleChildOf; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleChildOf1174); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleChildOf"


    // $ANTLR start "ruleChildOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:503:1: ruleChildOf returns [EObject current=null] : (this_LT_EM_0= RULE_LT_EM ( (lv_constraint_1_0= ruleFocusConcept ) ) ) ;
    public final EObject ruleChildOf() throws RecognitionException {
        EObject current = null;

        Token this_LT_EM_0=null;
        EObject lv_constraint_1_0 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:506:28: ( (this_LT_EM_0= RULE_LT_EM ( (lv_constraint_1_0= ruleFocusConcept ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:507:1: (this_LT_EM_0= RULE_LT_EM ( (lv_constraint_1_0= ruleFocusConcept ) ) )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:507:1: (this_LT_EM_0= RULE_LT_EM ( (lv_constraint_1_0= ruleFocusConcept ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:507:2: this_LT_EM_0= RULE_LT_EM ( (lv_constraint_1_0= ruleFocusConcept ) )
            {
            this_LT_EM_0=(Token)match(input,RULE_LT_EM,FOLLOW_RULE_LT_EM_in_ruleChildOf1210); 
             
                newLeafNode(this_LT_EM_0, grammarAccess.getChildOfAccess().getLT_EMTerminalRuleCall_0()); 
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:511:1: ( (lv_constraint_1_0= ruleFocusConcept ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:512:1: (lv_constraint_1_0= ruleFocusConcept )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:512:1: (lv_constraint_1_0= ruleFocusConcept )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:513:3: lv_constraint_1_0= ruleFocusConcept
            {
             
            	        newCompositeNode(grammarAccess.getChildOfAccess().getConstraintFocusConceptParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleFocusConcept_in_ruleChildOf1230);
            lv_constraint_1_0=ruleFocusConcept();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getChildOfRule());
            	        }
                   		set(
                   			current, 
                   			"constraint",
                    		lv_constraint_1_0, 
                    		"FocusConcept");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleChildOf"


    // $ANTLR start "entryRuleDescendantOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:537:1: entryRuleDescendantOf returns [EObject current=null] : iv_ruleDescendantOf= ruleDescendantOf EOF ;
    public final EObject entryRuleDescendantOf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDescendantOf = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:538:2: (iv_ruleDescendantOf= ruleDescendantOf EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:539:2: iv_ruleDescendantOf= ruleDescendantOf EOF
            {
             newCompositeNode(grammarAccess.getDescendantOfRule()); 
            pushFollow(FOLLOW_ruleDescendantOf_in_entryRuleDescendantOf1266);
            iv_ruleDescendantOf=ruleDescendantOf();

            state._fsp--;

             current =iv_ruleDescendantOf; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleDescendantOf1276); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleDescendantOf"


    // $ANTLR start "ruleDescendantOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:546:1: ruleDescendantOf returns [EObject current=null] : (this_LT_0= RULE_LT ( (lv_constraint_1_0= ruleFocusConcept ) ) ) ;
    public final EObject ruleDescendantOf() throws RecognitionException {
        EObject current = null;

        Token this_LT_0=null;
        EObject lv_constraint_1_0 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:549:28: ( (this_LT_0= RULE_LT ( (lv_constraint_1_0= ruleFocusConcept ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:550:1: (this_LT_0= RULE_LT ( (lv_constraint_1_0= ruleFocusConcept ) ) )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:550:1: (this_LT_0= RULE_LT ( (lv_constraint_1_0= ruleFocusConcept ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:550:2: this_LT_0= RULE_LT ( (lv_constraint_1_0= ruleFocusConcept ) )
            {
            this_LT_0=(Token)match(input,RULE_LT,FOLLOW_RULE_LT_in_ruleDescendantOf1312); 
             
                newLeafNode(this_LT_0, grammarAccess.getDescendantOfAccess().getLTTerminalRuleCall_0()); 
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:554:1: ( (lv_constraint_1_0= ruleFocusConcept ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:555:1: (lv_constraint_1_0= ruleFocusConcept )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:555:1: (lv_constraint_1_0= ruleFocusConcept )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:556:3: lv_constraint_1_0= ruleFocusConcept
            {
             
            	        newCompositeNode(grammarAccess.getDescendantOfAccess().getConstraintFocusConceptParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleFocusConcept_in_ruleDescendantOf1332);
            lv_constraint_1_0=ruleFocusConcept();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getDescendantOfRule());
            	        }
                   		set(
                   			current, 
                   			"constraint",
                    		lv_constraint_1_0, 
                    		"FocusConcept");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleDescendantOf"


    // $ANTLR start "entryRuleDescendantOrSelfOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:580:1: entryRuleDescendantOrSelfOf returns [EObject current=null] : iv_ruleDescendantOrSelfOf= ruleDescendantOrSelfOf EOF ;
    public final EObject entryRuleDescendantOrSelfOf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDescendantOrSelfOf = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:581:2: (iv_ruleDescendantOrSelfOf= ruleDescendantOrSelfOf EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:582:2: iv_ruleDescendantOrSelfOf= ruleDescendantOrSelfOf EOF
            {
             newCompositeNode(grammarAccess.getDescendantOrSelfOfRule()); 
            pushFollow(FOLLOW_ruleDescendantOrSelfOf_in_entryRuleDescendantOrSelfOf1368);
            iv_ruleDescendantOrSelfOf=ruleDescendantOrSelfOf();

            state._fsp--;

             current =iv_ruleDescendantOrSelfOf; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleDescendantOrSelfOf1378); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleDescendantOrSelfOf"


    // $ANTLR start "ruleDescendantOrSelfOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:589:1: ruleDescendantOrSelfOf returns [EObject current=null] : (this_DBL_LT_0= RULE_DBL_LT ( (lv_constraint_1_0= ruleFocusConcept ) ) ) ;
    public final EObject ruleDescendantOrSelfOf() throws RecognitionException {
        EObject current = null;

        Token this_DBL_LT_0=null;
        EObject lv_constraint_1_0 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:592:28: ( (this_DBL_LT_0= RULE_DBL_LT ( (lv_constraint_1_0= ruleFocusConcept ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:593:1: (this_DBL_LT_0= RULE_DBL_LT ( (lv_constraint_1_0= ruleFocusConcept ) ) )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:593:1: (this_DBL_LT_0= RULE_DBL_LT ( (lv_constraint_1_0= ruleFocusConcept ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:593:2: this_DBL_LT_0= RULE_DBL_LT ( (lv_constraint_1_0= ruleFocusConcept ) )
            {
            this_DBL_LT_0=(Token)match(input,RULE_DBL_LT,FOLLOW_RULE_DBL_LT_in_ruleDescendantOrSelfOf1414); 
             
                newLeafNode(this_DBL_LT_0, grammarAccess.getDescendantOrSelfOfAccess().getDBL_LTTerminalRuleCall_0()); 
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:597:1: ( (lv_constraint_1_0= ruleFocusConcept ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:598:1: (lv_constraint_1_0= ruleFocusConcept )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:598:1: (lv_constraint_1_0= ruleFocusConcept )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:599:3: lv_constraint_1_0= ruleFocusConcept
            {
             
            	        newCompositeNode(grammarAccess.getDescendantOrSelfOfAccess().getConstraintFocusConceptParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleFocusConcept_in_ruleDescendantOrSelfOf1434);
            lv_constraint_1_0=ruleFocusConcept();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getDescendantOrSelfOfRule());
            	        }
                   		set(
                   			current, 
                   			"constraint",
                    		lv_constraint_1_0, 
                    		"FocusConcept");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleDescendantOrSelfOf"


    // $ANTLR start "entryRuleParentOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:623:1: entryRuleParentOf returns [EObject current=null] : iv_ruleParentOf= ruleParentOf EOF ;
    public final EObject entryRuleParentOf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleParentOf = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:624:2: (iv_ruleParentOf= ruleParentOf EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:625:2: iv_ruleParentOf= ruleParentOf EOF
            {
             newCompositeNode(grammarAccess.getParentOfRule()); 
            pushFollow(FOLLOW_ruleParentOf_in_entryRuleParentOf1470);
            iv_ruleParentOf=ruleParentOf();

            state._fsp--;

             current =iv_ruleParentOf; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleParentOf1480); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleParentOf"


    // $ANTLR start "ruleParentOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:632:1: ruleParentOf returns [EObject current=null] : (this_GT_EM_0= RULE_GT_EM ( (lv_constraint_1_0= ruleFocusConcept ) ) ) ;
    public final EObject ruleParentOf() throws RecognitionException {
        EObject current = null;

        Token this_GT_EM_0=null;
        EObject lv_constraint_1_0 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:635:28: ( (this_GT_EM_0= RULE_GT_EM ( (lv_constraint_1_0= ruleFocusConcept ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:636:1: (this_GT_EM_0= RULE_GT_EM ( (lv_constraint_1_0= ruleFocusConcept ) ) )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:636:1: (this_GT_EM_0= RULE_GT_EM ( (lv_constraint_1_0= ruleFocusConcept ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:636:2: this_GT_EM_0= RULE_GT_EM ( (lv_constraint_1_0= ruleFocusConcept ) )
            {
            this_GT_EM_0=(Token)match(input,RULE_GT_EM,FOLLOW_RULE_GT_EM_in_ruleParentOf1516); 
             
                newLeafNode(this_GT_EM_0, grammarAccess.getParentOfAccess().getGT_EMTerminalRuleCall_0()); 
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:640:1: ( (lv_constraint_1_0= ruleFocusConcept ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:641:1: (lv_constraint_1_0= ruleFocusConcept )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:641:1: (lv_constraint_1_0= ruleFocusConcept )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:642:3: lv_constraint_1_0= ruleFocusConcept
            {
             
            	        newCompositeNode(grammarAccess.getParentOfAccess().getConstraintFocusConceptParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleFocusConcept_in_ruleParentOf1536);
            lv_constraint_1_0=ruleFocusConcept();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getParentOfRule());
            	        }
                   		set(
                   			current, 
                   			"constraint",
                    		lv_constraint_1_0, 
                    		"FocusConcept");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleParentOf"


    // $ANTLR start "entryRuleAncestorOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:666:1: entryRuleAncestorOf returns [EObject current=null] : iv_ruleAncestorOf= ruleAncestorOf EOF ;
    public final EObject entryRuleAncestorOf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAncestorOf = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:667:2: (iv_ruleAncestorOf= ruleAncestorOf EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:668:2: iv_ruleAncestorOf= ruleAncestorOf EOF
            {
             newCompositeNode(grammarAccess.getAncestorOfRule()); 
            pushFollow(FOLLOW_ruleAncestorOf_in_entryRuleAncestorOf1572);
            iv_ruleAncestorOf=ruleAncestorOf();

            state._fsp--;

             current =iv_ruleAncestorOf; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleAncestorOf1582); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAncestorOf"


    // $ANTLR start "ruleAncestorOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:675:1: ruleAncestorOf returns [EObject current=null] : (this_GT_0= RULE_GT ( (lv_constraint_1_0= ruleFocusConcept ) ) ) ;
    public final EObject ruleAncestorOf() throws RecognitionException {
        EObject current = null;

        Token this_GT_0=null;
        EObject lv_constraint_1_0 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:678:28: ( (this_GT_0= RULE_GT ( (lv_constraint_1_0= ruleFocusConcept ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:679:1: (this_GT_0= RULE_GT ( (lv_constraint_1_0= ruleFocusConcept ) ) )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:679:1: (this_GT_0= RULE_GT ( (lv_constraint_1_0= ruleFocusConcept ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:679:2: this_GT_0= RULE_GT ( (lv_constraint_1_0= ruleFocusConcept ) )
            {
            this_GT_0=(Token)match(input,RULE_GT,FOLLOW_RULE_GT_in_ruleAncestorOf1618); 
             
                newLeafNode(this_GT_0, grammarAccess.getAncestorOfAccess().getGTTerminalRuleCall_0()); 
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:683:1: ( (lv_constraint_1_0= ruleFocusConcept ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:684:1: (lv_constraint_1_0= ruleFocusConcept )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:684:1: (lv_constraint_1_0= ruleFocusConcept )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:685:3: lv_constraint_1_0= ruleFocusConcept
            {
             
            	        newCompositeNode(grammarAccess.getAncestorOfAccess().getConstraintFocusConceptParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleFocusConcept_in_ruleAncestorOf1638);
            lv_constraint_1_0=ruleFocusConcept();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getAncestorOfRule());
            	        }
                   		set(
                   			current, 
                   			"constraint",
                    		lv_constraint_1_0, 
                    		"FocusConcept");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAncestorOf"


    // $ANTLR start "entryRuleAncestorOrSelfOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:709:1: entryRuleAncestorOrSelfOf returns [EObject current=null] : iv_ruleAncestorOrSelfOf= ruleAncestorOrSelfOf EOF ;
    public final EObject entryRuleAncestorOrSelfOf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAncestorOrSelfOf = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:710:2: (iv_ruleAncestorOrSelfOf= ruleAncestorOrSelfOf EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:711:2: iv_ruleAncestorOrSelfOf= ruleAncestorOrSelfOf EOF
            {
             newCompositeNode(grammarAccess.getAncestorOrSelfOfRule()); 
            pushFollow(FOLLOW_ruleAncestorOrSelfOf_in_entryRuleAncestorOrSelfOf1674);
            iv_ruleAncestorOrSelfOf=ruleAncestorOrSelfOf();

            state._fsp--;

             current =iv_ruleAncestorOrSelfOf; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleAncestorOrSelfOf1684); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAncestorOrSelfOf"


    // $ANTLR start "ruleAncestorOrSelfOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:718:1: ruleAncestorOrSelfOf returns [EObject current=null] : (this_DBL_GT_0= RULE_DBL_GT ( (lv_constraint_1_0= ruleFocusConcept ) ) ) ;
    public final EObject ruleAncestorOrSelfOf() throws RecognitionException {
        EObject current = null;

        Token this_DBL_GT_0=null;
        EObject lv_constraint_1_0 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:721:28: ( (this_DBL_GT_0= RULE_DBL_GT ( (lv_constraint_1_0= ruleFocusConcept ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:722:1: (this_DBL_GT_0= RULE_DBL_GT ( (lv_constraint_1_0= ruleFocusConcept ) ) )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:722:1: (this_DBL_GT_0= RULE_DBL_GT ( (lv_constraint_1_0= ruleFocusConcept ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:722:2: this_DBL_GT_0= RULE_DBL_GT ( (lv_constraint_1_0= ruleFocusConcept ) )
            {
            this_DBL_GT_0=(Token)match(input,RULE_DBL_GT,FOLLOW_RULE_DBL_GT_in_ruleAncestorOrSelfOf1720); 
             
                newLeafNode(this_DBL_GT_0, grammarAccess.getAncestorOrSelfOfAccess().getDBL_GTTerminalRuleCall_0()); 
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:726:1: ( (lv_constraint_1_0= ruleFocusConcept ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:727:1: (lv_constraint_1_0= ruleFocusConcept )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:727:1: (lv_constraint_1_0= ruleFocusConcept )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:728:3: lv_constraint_1_0= ruleFocusConcept
            {
             
            	        newCompositeNode(grammarAccess.getAncestorOrSelfOfAccess().getConstraintFocusConceptParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleFocusConcept_in_ruleAncestorOrSelfOf1740);
            lv_constraint_1_0=ruleFocusConcept();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getAncestorOrSelfOfRule());
            	        }
                   		set(
                   			current, 
                   			"constraint",
                    		lv_constraint_1_0, 
                    		"FocusConcept");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAncestorOrSelfOf"


    // $ANTLR start "entryRuleMemberOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:752:1: entryRuleMemberOf returns [EObject current=null] : iv_ruleMemberOf= ruleMemberOf EOF ;
    public final EObject entryRuleMemberOf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMemberOf = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:753:2: (iv_ruleMemberOf= ruleMemberOf EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:754:2: iv_ruleMemberOf= ruleMemberOf EOF
            {
             newCompositeNode(grammarAccess.getMemberOfRule()); 
            pushFollow(FOLLOW_ruleMemberOf_in_entryRuleMemberOf1776);
            iv_ruleMemberOf=ruleMemberOf();

            state._fsp--;

             current =iv_ruleMemberOf; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleMemberOf1786); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMemberOf"


    // $ANTLR start "ruleMemberOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:761:1: ruleMemberOf returns [EObject current=null] : (this_CARET_0= RULE_CARET ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) ) ) ;
    public final EObject ruleMemberOf() throws RecognitionException {
        EObject current = null;

        Token this_CARET_0=null;
        EObject lv_constraint_1_1 = null;

        EObject lv_constraint_1_2 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:764:28: ( (this_CARET_0= RULE_CARET ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:765:1: (this_CARET_0= RULE_CARET ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) ) )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:765:1: (this_CARET_0= RULE_CARET ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:765:2: this_CARET_0= RULE_CARET ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) )
            {
            this_CARET_0=(Token)match(input,RULE_CARET,FOLLOW_RULE_CARET_in_ruleMemberOf1822); 
             
                newLeafNode(this_CARET_0, grammarAccess.getMemberOfAccess().getCARETTerminalRuleCall_0()); 
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:769:1: ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:770:1: ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:770:1: ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:771:1: (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:771:1: (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==RULE_DIGIT_NONZERO) ) {
                alt8=1;
            }
            else if ( (LA8_0==RULE_WILDCARD) ) {
                alt8=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:772:3: lv_constraint_1_1= ruleConceptReference
                    {
                     
                    	        newCompositeNode(grammarAccess.getMemberOfAccess().getConstraintConceptReferenceParserRuleCall_1_0_0()); 
                    	    
                    pushFollow(FOLLOW_ruleConceptReference_in_ruleMemberOf1844);
                    lv_constraint_1_1=ruleConceptReference();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getMemberOfRule());
                    	        }
                           		set(
                           			current, 
                           			"constraint",
                            		lv_constraint_1_1, 
                            		"ConceptReference");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }
                    break;
                case 2 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:787:8: lv_constraint_1_2= ruleAny
                    {
                     
                    	        newCompositeNode(grammarAccess.getMemberOfAccess().getConstraintAnyParserRuleCall_1_0_1()); 
                    	    
                    pushFollow(FOLLOW_ruleAny_in_ruleMemberOf1863);
                    lv_constraint_1_2=ruleAny();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getMemberOfRule());
                    	        }
                           		set(
                           			current, 
                           			"constraint",
                            		lv_constraint_1_2, 
                            		"Any");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }
                    break;

            }


            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMemberOf"


    // $ANTLR start "entryRuleConceptReference"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:813:1: entryRuleConceptReference returns [EObject current=null] : iv_ruleConceptReference= ruleConceptReference EOF ;
    public final EObject entryRuleConceptReference() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleConceptReference = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:814:2: (iv_ruleConceptReference= ruleConceptReference EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:815:2: iv_ruleConceptReference= ruleConceptReference EOF
            {
             newCompositeNode(grammarAccess.getConceptReferenceRule()); 
            pushFollow(FOLLOW_ruleConceptReference_in_entryRuleConceptReference1902);
            iv_ruleConceptReference=ruleConceptReference();

            state._fsp--;

             current =iv_ruleConceptReference; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleConceptReference1912); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleConceptReference"


    // $ANTLR start "ruleConceptReference"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:822:1: ruleConceptReference returns [EObject current=null] : ( ( (lv_id_0_0= ruleSnomedIdentifier ) ) (this_PIPE_1= RULE_PIPE ( (lv_term_2_0= ruleTerm ) ) this_PIPE_3= RULE_PIPE )? ) ;
    public final EObject ruleConceptReference() throws RecognitionException {
        EObject current = null;

        Token this_PIPE_1=null;
        Token this_PIPE_3=null;
        AntlrDatatypeRuleToken lv_id_0_0 = null;

        AntlrDatatypeRuleToken lv_term_2_0 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:825:28: ( ( ( (lv_id_0_0= ruleSnomedIdentifier ) ) (this_PIPE_1= RULE_PIPE ( (lv_term_2_0= ruleTerm ) ) this_PIPE_3= RULE_PIPE )? ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:826:1: ( ( (lv_id_0_0= ruleSnomedIdentifier ) ) (this_PIPE_1= RULE_PIPE ( (lv_term_2_0= ruleTerm ) ) this_PIPE_3= RULE_PIPE )? )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:826:1: ( ( (lv_id_0_0= ruleSnomedIdentifier ) ) (this_PIPE_1= RULE_PIPE ( (lv_term_2_0= ruleTerm ) ) this_PIPE_3= RULE_PIPE )? )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:826:2: ( (lv_id_0_0= ruleSnomedIdentifier ) ) (this_PIPE_1= RULE_PIPE ( (lv_term_2_0= ruleTerm ) ) this_PIPE_3= RULE_PIPE )?
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:826:2: ( (lv_id_0_0= ruleSnomedIdentifier ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:827:1: (lv_id_0_0= ruleSnomedIdentifier )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:827:1: (lv_id_0_0= ruleSnomedIdentifier )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:828:3: lv_id_0_0= ruleSnomedIdentifier
            {
             
            	        newCompositeNode(grammarAccess.getConceptReferenceAccess().getIdSnomedIdentifierParserRuleCall_0_0()); 
            	    
            pushFollow(FOLLOW_ruleSnomedIdentifier_in_ruleConceptReference1958);
            lv_id_0_0=ruleSnomedIdentifier();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getConceptReferenceRule());
            	        }
                   		set(
                   			current, 
                   			"id",
                    		lv_id_0_0, 
                    		"SnomedIdentifier");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:844:2: (this_PIPE_1= RULE_PIPE ( (lv_term_2_0= ruleTerm ) ) this_PIPE_3= RULE_PIPE )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==RULE_PIPE) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:844:3: this_PIPE_1= RULE_PIPE ( (lv_term_2_0= ruleTerm ) ) this_PIPE_3= RULE_PIPE
                    {
                    this_PIPE_1=(Token)match(input,RULE_PIPE,FOLLOW_RULE_PIPE_in_ruleConceptReference1970); 
                     
                        newLeafNode(this_PIPE_1, grammarAccess.getConceptReferenceAccess().getPIPETerminalRuleCall_1_0()); 
                        
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:848:1: ( (lv_term_2_0= ruleTerm ) )
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:849:1: (lv_term_2_0= ruleTerm )
                    {
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:849:1: (lv_term_2_0= ruleTerm )
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:850:3: lv_term_2_0= ruleTerm
                    {
                     
                    	        newCompositeNode(grammarAccess.getConceptReferenceAccess().getTermTermParserRuleCall_1_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleTerm_in_ruleConceptReference1990);
                    lv_term_2_0=ruleTerm();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getConceptReferenceRule());
                    	        }
                           		set(
                           			current, 
                           			"term",
                            		lv_term_2_0, 
                            		"Term");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }

                    this_PIPE_3=(Token)match(input,RULE_PIPE,FOLLOW_RULE_PIPE_in_ruleConceptReference2001); 
                     
                        newLeafNode(this_PIPE_3, grammarAccess.getConceptReferenceAccess().getPIPETerminalRuleCall_1_2()); 
                        

                    }
                    break;

            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleConceptReference"


    // $ANTLR start "entryRuleAny"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:878:1: entryRuleAny returns [EObject current=null] : iv_ruleAny= ruleAny EOF ;
    public final EObject entryRuleAny() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAny = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:879:2: (iv_ruleAny= ruleAny EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:880:2: iv_ruleAny= ruleAny EOF
            {
             newCompositeNode(grammarAccess.getAnyRule()); 
            pushFollow(FOLLOW_ruleAny_in_entryRuleAny2038);
            iv_ruleAny=ruleAny();

            state._fsp--;

             current =iv_ruleAny; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleAny2048); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAny"


    // $ANTLR start "ruleAny"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:887:1: ruleAny returns [EObject current=null] : (this_WILDCARD_0= RULE_WILDCARD () ) ;
    public final EObject ruleAny() throws RecognitionException {
        EObject current = null;

        Token this_WILDCARD_0=null;

         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:890:28: ( (this_WILDCARD_0= RULE_WILDCARD () ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:891:1: (this_WILDCARD_0= RULE_WILDCARD () )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:891:1: (this_WILDCARD_0= RULE_WILDCARD () )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:891:2: this_WILDCARD_0= RULE_WILDCARD ()
            {
            this_WILDCARD_0=(Token)match(input,RULE_WILDCARD,FOLLOW_RULE_WILDCARD_in_ruleAny2084); 
             
                newLeafNode(this_WILDCARD_0, grammarAccess.getAnyAccess().getWILDCARDTerminalRuleCall_0()); 
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:895:1: ()
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:896:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getAnyAccess().getAnyAction_1(),
                        current);
                

            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAny"


    // $ANTLR start "entryRuleRefinement"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:909:1: entryRuleRefinement returns [EObject current=null] : iv_ruleRefinement= ruleRefinement EOF ;
    public final EObject entryRuleRefinement() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRefinement = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:910:2: (iv_ruleRefinement= ruleRefinement EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:911:2: iv_ruleRefinement= ruleRefinement EOF
            {
             newCompositeNode(grammarAccess.getRefinementRule()); 
            pushFollow(FOLLOW_ruleRefinement_in_entryRuleRefinement2128);
            iv_ruleRefinement=ruleRefinement();

            state._fsp--;

             current =iv_ruleRefinement; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleRefinement2138); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRefinement"


    // $ANTLR start "ruleRefinement"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:918:1: ruleRefinement returns [EObject current=null] : this_AttributeConstraint_0= ruleAttributeConstraint ;
    public final EObject ruleRefinement() throws RecognitionException {
        EObject current = null;

        EObject this_AttributeConstraint_0 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:921:28: (this_AttributeConstraint_0= ruleAttributeConstraint )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:923:5: this_AttributeConstraint_0= ruleAttributeConstraint
            {
             
                    newCompositeNode(grammarAccess.getRefinementAccess().getAttributeConstraintParserRuleCall()); 
                
            pushFollow(FOLLOW_ruleAttributeConstraint_in_ruleRefinement2184);
            this_AttributeConstraint_0=ruleAttributeConstraint();

            state._fsp--;

             
                    current = this_AttributeConstraint_0; 
                    afterParserOrEnumRuleCall();
                

            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRefinement"


    // $ANTLR start "entryRuleAttributeConstraint"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:939:1: entryRuleAttributeConstraint returns [EObject current=null] : iv_ruleAttributeConstraint= ruleAttributeConstraint EOF ;
    public final EObject entryRuleAttributeConstraint() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAttributeConstraint = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:940:2: (iv_ruleAttributeConstraint= ruleAttributeConstraint EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:941:2: iv_ruleAttributeConstraint= ruleAttributeConstraint EOF
            {
             newCompositeNode(grammarAccess.getAttributeConstraintRule()); 
            pushFollow(FOLLOW_ruleAttributeConstraint_in_entryRuleAttributeConstraint2218);
            iv_ruleAttributeConstraint=ruleAttributeConstraint();

            state._fsp--;

             current =iv_ruleAttributeConstraint; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleAttributeConstraint2228); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAttributeConstraint"


    // $ANTLR start "ruleAttributeConstraint"
<<<<<<< feature/ecl-support
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:948:1: ruleAttributeConstraint returns [EObject current=null] : ( ( (lv_reversed_0_0= RULE_REVERSED ) )? ( ( (lv_attribute_1_1= ruleConceptReference | lv_attribute_1_2= ruleAny ) ) ) ( (lv_comparison_2_0= ruleComparison ) ) ) ;
=======
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:948:1: ruleAttributeConstraint returns [EObject current=null] : ( ( (lv_cardinality_0_0= ruleCardinality ) )? ( (lv_reversed_1_0= RULE_REVERSED ) )? ( (lv_attribute_2_0= ruleAttribute ) ) ( (lv_comparison_3_0= ruleComparison ) ) ) ;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public final EObject ruleAttributeConstraint() throws RecognitionException {
        EObject current = null;

        Token lv_reversed_0_0=null;
        EObject lv_attribute_1_1 = null;

<<<<<<< feature/ecl-support
        EObject lv_attribute_1_2 = null;

        EObject lv_comparison_2_0 = null;
=======
        EObject lv_attribute_2_0 = null;

        EObject lv_comparison_3_0 = null;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...


         enterRule(); 
            
        try {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:951:28: ( ( ( (lv_reversed_0_0= RULE_REVERSED ) )? ( ( (lv_attribute_1_1= ruleConceptReference | lv_attribute_1_2= ruleAny ) ) ) ( (lv_comparison_2_0= ruleComparison ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:952:1: ( ( (lv_reversed_0_0= RULE_REVERSED ) )? ( ( (lv_attribute_1_1= ruleConceptReference | lv_attribute_1_2= ruleAny ) ) ) ( (lv_comparison_2_0= ruleComparison ) ) )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:951:28: ( ( ( (lv_cardinality_0_0= ruleCardinality ) )? ( (lv_reversed_1_0= RULE_REVERSED ) )? ( (lv_attribute_2_0= ruleAttribute ) ) ( (lv_comparison_3_0= ruleComparison ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:952:1: ( ( (lv_cardinality_0_0= ruleCardinality ) )? ( (lv_reversed_1_0= RULE_REVERSED ) )? ( (lv_attribute_2_0= ruleAttribute ) ) ( (lv_comparison_3_0= ruleComparison ) ) )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:952:1: ( ( (lv_reversed_0_0= RULE_REVERSED ) )? ( ( (lv_attribute_1_1= ruleConceptReference | lv_attribute_1_2= ruleAny ) ) ) ( (lv_comparison_2_0= ruleComparison ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:952:2: ( (lv_reversed_0_0= RULE_REVERSED ) )? ( ( (lv_attribute_1_1= ruleConceptReference | lv_attribute_1_2= ruleAny ) ) ) ( (lv_comparison_2_0= ruleComparison ) )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:952:1: ( ( (lv_cardinality_0_0= ruleCardinality ) )? ( (lv_reversed_1_0= RULE_REVERSED ) )? ( (lv_attribute_2_0= ruleAttribute ) ) ( (lv_comparison_3_0= ruleComparison ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:952:2: ( (lv_cardinality_0_0= ruleCardinality ) )? ( (lv_reversed_1_0= RULE_REVERSED ) )? ( (lv_attribute_2_0= ruleAttribute ) ) ( (lv_comparison_3_0= ruleComparison ) )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:952:2: ( (lv_reversed_0_0= RULE_REVERSED ) )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==RULE_REVERSED) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:953:1: (lv_reversed_0_0= RULE_REVERSED )
                    {
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:953:1: (lv_reversed_0_0= RULE_REVERSED )
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:954:3: lv_reversed_0_0= RULE_REVERSED
                    {
                    lv_reversed_0_0=(Token)match(input,RULE_REVERSED,FOLLOW_RULE_REVERSED_in_ruleAttributeConstraint2270); 

                    			newLeafNode(lv_reversed_0_0, grammarAccess.getAttributeConstraintAccess().getReversedREVERSEDTerminalRuleCall_0_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getAttributeConstraintRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"reversed",
                            		true, 
                            		"REVERSED");
                    	    

                    }


                    }
                    break;

            }

<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:970:3: ( ( (lv_attribute_1_1= ruleConceptReference | lv_attribute_1_2= ruleAny ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:971:1: ( (lv_attribute_1_1= ruleConceptReference | lv_attribute_1_2= ruleAny ) )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:988:3: ( (lv_attribute_2_0= ruleAttribute ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:989:1: (lv_attribute_2_0= ruleAttribute )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:971:1: ( (lv_attribute_1_1= ruleConceptReference | lv_attribute_1_2= ruleAny ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:972:1: (lv_attribute_1_1= ruleConceptReference | lv_attribute_1_2= ruleAny )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:989:1: (lv_attribute_2_0= ruleAttribute )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:990:3: lv_attribute_2_0= ruleAttribute
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:972:1: (lv_attribute_1_1= ruleConceptReference | lv_attribute_1_2= ruleAny )
            int alt11=2;
            int LA11_0 = input.LA(1);
=======
             
            	        newCompositeNode(grammarAccess.getAttributeConstraintAccess().getAttributeAttributeParserRuleCall_2_0()); 
            	    
            pushFollow(FOLLOW_ruleAttribute_in_ruleAttributeConstraint2319);
            lv_attribute_2_0=ruleAttribute();
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

<<<<<<< feature/ecl-support
            if ( (LA11_0==RULE_DIGIT_NONZERO) ) {
                alt11=1;
            }
            else if ( (LA11_0==RULE_WILDCARD) ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:973:3: lv_attribute_1_1= ruleConceptReference
                    {
                     
                    	        newCompositeNode(grammarAccess.getAttributeConstraintAccess().getAttributeConceptReferenceParserRuleCall_1_0_0()); 
                    	    
                    pushFollow(FOLLOW_ruleConceptReference_in_ruleAttributeConstraint2299);
                    lv_attribute_1_1=ruleConceptReference();

                    state._fsp--;
=======
            state._fsp--;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...


<<<<<<< feature/ecl-support
                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getAttributeConstraintRule());
                    	        }
                           		set(
                           			current, 
                           			"attribute",
                            		lv_attribute_1_1, 
                            		"ConceptReference");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }
                    break;
                case 2 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:988:8: lv_attribute_1_2= ruleAny
                    {
                     
                    	        newCompositeNode(grammarAccess.getAttributeConstraintAccess().getAttributeAnyParserRuleCall_1_0_1()); 
                    	    
                    pushFollow(FOLLOW_ruleAny_in_ruleAttributeConstraint2318);
                    lv_attribute_1_2=ruleAny();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getAttributeConstraintRule());
                    	        }
                           		set(
                           			current, 
                           			"attribute",
                            		lv_attribute_1_2, 
                            		"Any");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }
                    break;
=======
            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getAttributeConstraintRule());
            	        }
                   		set(
                   			current, 
                   			"attribute",
                    		lv_attribute_2_0, 
                    		"Attribute");
            	        afterParserOrEnumRuleCall();
            	    
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

            }


            }

<<<<<<< feature/ecl-support

            }

            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1006:2: ( (lv_comparison_2_0= ruleComparison ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1007:1: (lv_comparison_2_0= ruleComparison )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1006:2: ( (lv_comparison_3_0= ruleComparison ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1007:1: (lv_comparison_3_0= ruleComparison )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1007:1: (lv_comparison_2_0= ruleComparison )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1008:3: lv_comparison_2_0= ruleComparison
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1007:1: (lv_comparison_3_0= ruleComparison )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1008:3: lv_comparison_3_0= ruleComparison
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
             
            	        newCompositeNode(grammarAccess.getAttributeConstraintAccess().getComparisonComparisonParserRuleCall_2_0()); 
            	    
<<<<<<< feature/ecl-support
            pushFollow(FOLLOW_ruleComparison_in_ruleAttributeConstraint2342);
            lv_comparison_2_0=ruleComparison();
=======
            pushFollow(FOLLOW_ruleComparison_in_ruleAttributeConstraint2340);
            lv_comparison_3_0=ruleComparison();
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getAttributeConstraintRule());
            	        }
                   		set(
                   			current, 
                   			"comparison",
                    		lv_comparison_2_0, 
                    		"Comparison");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAttributeConstraint"


<<<<<<< feature/ecl-support
=======
    // $ANTLR start "entryRuleAttribute"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1032:1: entryRuleAttribute returns [EObject current=null] : iv_ruleAttribute= ruleAttribute EOF ;
    public final EObject entryRuleAttribute() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAttribute = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1033:2: (iv_ruleAttribute= ruleAttribute EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1034:2: iv_ruleAttribute= ruleAttribute EOF
            {
             newCompositeNode(grammarAccess.getAttributeRule()); 
            pushFollow(FOLLOW_ruleAttribute_in_entryRuleAttribute2376);
            iv_ruleAttribute=ruleAttribute();

            state._fsp--;

             current =iv_ruleAttribute; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleAttribute2386); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAttribute"


    // $ANTLR start "ruleAttribute"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1041:1: ruleAttribute returns [EObject current=null] : (this_AttributeDescendantOf_0= ruleAttributeDescendantOf | this_AttributeDescendantOrSelfOf_1= ruleAttributeDescendantOrSelfOf | this_ConceptReference_2= ruleConceptReference | this_Any_3= ruleAny ) ;
    public final EObject ruleAttribute() throws RecognitionException {
        EObject current = null;

        EObject this_AttributeDescendantOf_0 = null;

        EObject this_AttributeDescendantOrSelfOf_1 = null;

        EObject this_ConceptReference_2 = null;

        EObject this_Any_3 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1044:28: ( (this_AttributeDescendantOf_0= ruleAttributeDescendantOf | this_AttributeDescendantOrSelfOf_1= ruleAttributeDescendantOrSelfOf | this_ConceptReference_2= ruleConceptReference | this_Any_3= ruleAny ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1045:1: (this_AttributeDescendantOf_0= ruleAttributeDescendantOf | this_AttributeDescendantOrSelfOf_1= ruleAttributeDescendantOrSelfOf | this_ConceptReference_2= ruleConceptReference | this_Any_3= ruleAny )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1045:1: (this_AttributeDescendantOf_0= ruleAttributeDescendantOf | this_AttributeDescendantOrSelfOf_1= ruleAttributeDescendantOrSelfOf | this_ConceptReference_2= ruleConceptReference | this_Any_3= ruleAny )
            int alt12=4;
            switch ( input.LA(1) ) {
            case RULE_LT:
                {
                alt12=1;
                }
                break;
            case RULE_DBL_LT:
                {
                alt12=2;
                }
                break;
            case RULE_DIGIT_NONZERO:
                {
                alt12=3;
                }
                break;
            case RULE_WILDCARD:
                {
                alt12=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }

            switch (alt12) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1046:5: this_AttributeDescendantOf_0= ruleAttributeDescendantOf
                    {
                     
                            newCompositeNode(grammarAccess.getAttributeAccess().getAttributeDescendantOfParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleAttributeDescendantOf_in_ruleAttribute2433);
                    this_AttributeDescendantOf_0=ruleAttributeDescendantOf();

                    state._fsp--;

                     
                            current = this_AttributeDescendantOf_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1056:5: this_AttributeDescendantOrSelfOf_1= ruleAttributeDescendantOrSelfOf
                    {
                     
                            newCompositeNode(grammarAccess.getAttributeAccess().getAttributeDescendantOrSelfOfParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleAttributeDescendantOrSelfOf_in_ruleAttribute2460);
                    this_AttributeDescendantOrSelfOf_1=ruleAttributeDescendantOrSelfOf();

                    state._fsp--;

                     
                            current = this_AttributeDescendantOrSelfOf_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 3 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1066:5: this_ConceptReference_2= ruleConceptReference
                    {
                     
                            newCompositeNode(grammarAccess.getAttributeAccess().getConceptReferenceParserRuleCall_2()); 
                        
                    pushFollow(FOLLOW_ruleConceptReference_in_ruleAttribute2487);
                    this_ConceptReference_2=ruleConceptReference();

                    state._fsp--;

                     
                            current = this_ConceptReference_2; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 4 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1076:5: this_Any_3= ruleAny
                    {
                     
                            newCompositeNode(grammarAccess.getAttributeAccess().getAnyParserRuleCall_3()); 
                        
                    pushFollow(FOLLOW_ruleAny_in_ruleAttribute2514);
                    this_Any_3=ruleAny();

                    state._fsp--;

                     
                            current = this_Any_3; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAttribute"


    // $ANTLR start "entryRuleAttributeDescendantOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1092:1: entryRuleAttributeDescendantOf returns [EObject current=null] : iv_ruleAttributeDescendantOf= ruleAttributeDescendantOf EOF ;
    public final EObject entryRuleAttributeDescendantOf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAttributeDescendantOf = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1093:2: (iv_ruleAttributeDescendantOf= ruleAttributeDescendantOf EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1094:2: iv_ruleAttributeDescendantOf= ruleAttributeDescendantOf EOF
            {
             newCompositeNode(grammarAccess.getAttributeDescendantOfRule()); 
            pushFollow(FOLLOW_ruleAttributeDescendantOf_in_entryRuleAttributeDescendantOf2549);
            iv_ruleAttributeDescendantOf=ruleAttributeDescendantOf();

            state._fsp--;

             current =iv_ruleAttributeDescendantOf; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleAttributeDescendantOf2559); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAttributeDescendantOf"


    // $ANTLR start "ruleAttributeDescendantOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1101:1: ruleAttributeDescendantOf returns [EObject current=null] : (this_LT_0= RULE_LT ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) ) ) ;
    public final EObject ruleAttributeDescendantOf() throws RecognitionException {
        EObject current = null;

        Token this_LT_0=null;
        EObject lv_constraint_1_1 = null;

        EObject lv_constraint_1_2 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1104:28: ( (this_LT_0= RULE_LT ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1105:1: (this_LT_0= RULE_LT ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) ) )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1105:1: (this_LT_0= RULE_LT ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1105:2: this_LT_0= RULE_LT ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) )
            {
            this_LT_0=(Token)match(input,RULE_LT,FOLLOW_RULE_LT_in_ruleAttributeDescendantOf2595); 
             
                newLeafNode(this_LT_0, grammarAccess.getAttributeDescendantOfAccess().getLTTerminalRuleCall_0()); 
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1109:1: ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1110:1: ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1110:1: ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1111:1: (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1111:1: (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==RULE_DIGIT_NONZERO) ) {
                alt13=1;
            }
            else if ( (LA13_0==RULE_WILDCARD) ) {
                alt13=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1112:3: lv_constraint_1_1= ruleConceptReference
                    {
                     
                    	        newCompositeNode(grammarAccess.getAttributeDescendantOfAccess().getConstraintConceptReferenceParserRuleCall_1_0_0()); 
                    	    
                    pushFollow(FOLLOW_ruleConceptReference_in_ruleAttributeDescendantOf2617);
                    lv_constraint_1_1=ruleConceptReference();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getAttributeDescendantOfRule());
                    	        }
                           		set(
                           			current, 
                           			"constraint",
                            		lv_constraint_1_1, 
                            		"ConceptReference");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }
                    break;
                case 2 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1127:8: lv_constraint_1_2= ruleAny
                    {
                     
                    	        newCompositeNode(grammarAccess.getAttributeDescendantOfAccess().getConstraintAnyParserRuleCall_1_0_1()); 
                    	    
                    pushFollow(FOLLOW_ruleAny_in_ruleAttributeDescendantOf2636);
                    lv_constraint_1_2=ruleAny();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getAttributeDescendantOfRule());
                    	        }
                           		set(
                           			current, 
                           			"constraint",
                            		lv_constraint_1_2, 
                            		"Any");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }
                    break;

            }


            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAttributeDescendantOf"


    // $ANTLR start "entryRuleAttributeDescendantOrSelfOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1153:1: entryRuleAttributeDescendantOrSelfOf returns [EObject current=null] : iv_ruleAttributeDescendantOrSelfOf= ruleAttributeDescendantOrSelfOf EOF ;
    public final EObject entryRuleAttributeDescendantOrSelfOf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAttributeDescendantOrSelfOf = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1154:2: (iv_ruleAttributeDescendantOrSelfOf= ruleAttributeDescendantOrSelfOf EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1155:2: iv_ruleAttributeDescendantOrSelfOf= ruleAttributeDescendantOrSelfOf EOF
            {
             newCompositeNode(grammarAccess.getAttributeDescendantOrSelfOfRule()); 
            pushFollow(FOLLOW_ruleAttributeDescendantOrSelfOf_in_entryRuleAttributeDescendantOrSelfOf2675);
            iv_ruleAttributeDescendantOrSelfOf=ruleAttributeDescendantOrSelfOf();

            state._fsp--;

             current =iv_ruleAttributeDescendantOrSelfOf; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleAttributeDescendantOrSelfOf2685); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAttributeDescendantOrSelfOf"


    // $ANTLR start "ruleAttributeDescendantOrSelfOf"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1162:1: ruleAttributeDescendantOrSelfOf returns [EObject current=null] : (this_DBL_LT_0= RULE_DBL_LT ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) ) ) ;
    public final EObject ruleAttributeDescendantOrSelfOf() throws RecognitionException {
        EObject current = null;

        Token this_DBL_LT_0=null;
        EObject lv_constraint_1_1 = null;

        EObject lv_constraint_1_2 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1165:28: ( (this_DBL_LT_0= RULE_DBL_LT ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1166:1: (this_DBL_LT_0= RULE_DBL_LT ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) ) )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1166:1: (this_DBL_LT_0= RULE_DBL_LT ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1166:2: this_DBL_LT_0= RULE_DBL_LT ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) )
            {
            this_DBL_LT_0=(Token)match(input,RULE_DBL_LT,FOLLOW_RULE_DBL_LT_in_ruleAttributeDescendantOrSelfOf2721); 
             
                newLeafNode(this_DBL_LT_0, grammarAccess.getAttributeDescendantOrSelfOfAccess().getDBL_LTTerminalRuleCall_0()); 
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1170:1: ( ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1171:1: ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1171:1: ( (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1172:1: (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1172:1: (lv_constraint_1_1= ruleConceptReference | lv_constraint_1_2= ruleAny )
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==RULE_DIGIT_NONZERO) ) {
                alt14=1;
            }
            else if ( (LA14_0==RULE_WILDCARD) ) {
                alt14=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;
            }
            switch (alt14) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1173:3: lv_constraint_1_1= ruleConceptReference
                    {
                     
                    	        newCompositeNode(grammarAccess.getAttributeDescendantOrSelfOfAccess().getConstraintConceptReferenceParserRuleCall_1_0_0()); 
                    	    
                    pushFollow(FOLLOW_ruleConceptReference_in_ruleAttributeDescendantOrSelfOf2743);
                    lv_constraint_1_1=ruleConceptReference();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getAttributeDescendantOrSelfOfRule());
                    	        }
                           		set(
                           			current, 
                           			"constraint",
                            		lv_constraint_1_1, 
                            		"ConceptReference");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }
                    break;
                case 2 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1188:8: lv_constraint_1_2= ruleAny
                    {
                     
                    	        newCompositeNode(grammarAccess.getAttributeDescendantOrSelfOfAccess().getConstraintAnyParserRuleCall_1_0_1()); 
                    	    
                    pushFollow(FOLLOW_ruleAny_in_ruleAttributeDescendantOrSelfOf2762);
                    lv_constraint_1_2=ruleAny();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getAttributeDescendantOrSelfOfRule());
                    	        }
                           		set(
                           			current, 
                           			"constraint",
                            		lv_constraint_1_2, 
                            		"Any");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }
                    break;

            }


            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAttributeDescendantOrSelfOf"


    // $ANTLR start "entryRuleCardinality"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1214:1: entryRuleCardinality returns [EObject current=null] : iv_ruleCardinality= ruleCardinality EOF ;
    public final EObject entryRuleCardinality() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleCardinality = null;


        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1215:2: (iv_ruleCardinality= ruleCardinality EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1216:2: iv_ruleCardinality= ruleCardinality EOF
            {
             newCompositeNode(grammarAccess.getCardinalityRule()); 
            pushFollow(FOLLOW_ruleCardinality_in_entryRuleCardinality2801);
            iv_ruleCardinality=ruleCardinality();

            state._fsp--;

             current =iv_ruleCardinality; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleCardinality2811); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleCardinality"


    // $ANTLR start "ruleCardinality"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1223:1: ruleCardinality returns [EObject current=null] : (this_SQUARE_OPEN_0= RULE_SQUARE_OPEN ( (lv_min_1_0= ruleNonNegativeInteger ) ) this_TO_2= RULE_TO ( (lv_max_3_0= ruleMaxValue ) ) this_SQUARE_CLOSE_4= RULE_SQUARE_CLOSE ) ;
    public final EObject ruleCardinality() throws RecognitionException {
        EObject current = null;

        Token this_SQUARE_OPEN_0=null;
        Token this_TO_2=null;
        Token this_SQUARE_CLOSE_4=null;
        AntlrDatatypeRuleToken lv_min_1_0 = null;

        AntlrDatatypeRuleToken lv_max_3_0 = null;


         enterRule(); 
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1226:28: ( (this_SQUARE_OPEN_0= RULE_SQUARE_OPEN ( (lv_min_1_0= ruleNonNegativeInteger ) ) this_TO_2= RULE_TO ( (lv_max_3_0= ruleMaxValue ) ) this_SQUARE_CLOSE_4= RULE_SQUARE_CLOSE ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1227:1: (this_SQUARE_OPEN_0= RULE_SQUARE_OPEN ( (lv_min_1_0= ruleNonNegativeInteger ) ) this_TO_2= RULE_TO ( (lv_max_3_0= ruleMaxValue ) ) this_SQUARE_CLOSE_4= RULE_SQUARE_CLOSE )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1227:1: (this_SQUARE_OPEN_0= RULE_SQUARE_OPEN ( (lv_min_1_0= ruleNonNegativeInteger ) ) this_TO_2= RULE_TO ( (lv_max_3_0= ruleMaxValue ) ) this_SQUARE_CLOSE_4= RULE_SQUARE_CLOSE )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1227:2: this_SQUARE_OPEN_0= RULE_SQUARE_OPEN ( (lv_min_1_0= ruleNonNegativeInteger ) ) this_TO_2= RULE_TO ( (lv_max_3_0= ruleMaxValue ) ) this_SQUARE_CLOSE_4= RULE_SQUARE_CLOSE
            {
            this_SQUARE_OPEN_0=(Token)match(input,RULE_SQUARE_OPEN,FOLLOW_RULE_SQUARE_OPEN_in_ruleCardinality2847); 
             
                newLeafNode(this_SQUARE_OPEN_0, grammarAccess.getCardinalityAccess().getSQUARE_OPENTerminalRuleCall_0()); 
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1231:1: ( (lv_min_1_0= ruleNonNegativeInteger ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1232:1: (lv_min_1_0= ruleNonNegativeInteger )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1232:1: (lv_min_1_0= ruleNonNegativeInteger )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1233:3: lv_min_1_0= ruleNonNegativeInteger
            {
             
            	        newCompositeNode(grammarAccess.getCardinalityAccess().getMinNonNegativeIntegerParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleNonNegativeInteger_in_ruleCardinality2867);
            lv_min_1_0=ruleNonNegativeInteger();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getCardinalityRule());
            	        }
                   		set(
                   			current, 
                   			"min",
                    		lv_min_1_0, 
                    		"NonNegativeInteger");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            this_TO_2=(Token)match(input,RULE_TO,FOLLOW_RULE_TO_in_ruleCardinality2878); 
             
                newLeafNode(this_TO_2, grammarAccess.getCardinalityAccess().getTOTerminalRuleCall_2()); 
                
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1253:1: ( (lv_max_3_0= ruleMaxValue ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1254:1: (lv_max_3_0= ruleMaxValue )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1254:1: (lv_max_3_0= ruleMaxValue )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1255:3: lv_max_3_0= ruleMaxValue
            {
             
            	        newCompositeNode(grammarAccess.getCardinalityAccess().getMaxMaxValueParserRuleCall_3_0()); 
            	    
            pushFollow(FOLLOW_ruleMaxValue_in_ruleCardinality2898);
            lv_max_3_0=ruleMaxValue();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getCardinalityRule());
            	        }
                   		set(
                   			current, 
                   			"max",
                    		lv_max_3_0, 
                    		"MaxValue");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            this_SQUARE_CLOSE_4=(Token)match(input,RULE_SQUARE_CLOSE,FOLLOW_RULE_SQUARE_CLOSE_in_ruleCardinality2909); 
             
                newLeafNode(this_SQUARE_CLOSE_4, grammarAccess.getCardinalityAccess().getSQUARE_CLOSETerminalRuleCall_4()); 
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleCardinality"


>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    // $ANTLR start "entryRuleComparison"
<<<<<<< feature/ecl-support
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1032:1: entryRuleComparison returns [EObject current=null] : iv_ruleComparison= ruleComparison EOF ;
=======
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1283:1: entryRuleComparison returns [EObject current=null] : iv_ruleComparison= ruleComparison EOF ;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public final EObject entryRuleComparison() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleComparison = null;


        try {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1033:2: (iv_ruleComparison= ruleComparison EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1034:2: iv_ruleComparison= ruleComparison EOF
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1284:2: (iv_ruleComparison= ruleComparison EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1285:2: iv_ruleComparison= ruleComparison EOF
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
             newCompositeNode(grammarAccess.getComparisonRule()); 
<<<<<<< feature/ecl-support
            pushFollow(FOLLOW_ruleComparison_in_entryRuleComparison2378);
=======
            pushFollow(FOLLOW_ruleComparison_in_entryRuleComparison2944);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            iv_ruleComparison=ruleComparison();

            state._fsp--;

             current =iv_ruleComparison; 
<<<<<<< feature/ecl-support
            match(input,EOF,FOLLOW_EOF_in_entryRuleComparison2388); 
=======
            match(input,EOF,FOLLOW_EOF_in_entryRuleComparison2954); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleComparison"


    // $ANTLR start "ruleComparison"
<<<<<<< feature/ecl-support
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1041:1: ruleComparison returns [EObject current=null] : (this_AttributeValueEquals_0= ruleAttributeValueEquals | this_AttributeValueNotEquals_1= ruleAttributeValueNotEquals ) ;
=======
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1292:1: ruleComparison returns [EObject current=null] : (this_AttributeValueEquals_0= ruleAttributeValueEquals | this_AttributeValueNotEquals_1= ruleAttributeValueNotEquals ) ;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public final EObject ruleComparison() throws RecognitionException {
        EObject current = null;

        EObject this_AttributeValueEquals_0 = null;

        EObject this_AttributeValueNotEquals_1 = null;


         enterRule(); 
            
        try {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1044:28: ( (this_AttributeValueEquals_0= ruleAttributeValueEquals | this_AttributeValueNotEquals_1= ruleAttributeValueNotEquals ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1045:1: (this_AttributeValueEquals_0= ruleAttributeValueEquals | this_AttributeValueNotEquals_1= ruleAttributeValueNotEquals )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1295:28: ( (this_AttributeValueEquals_0= ruleAttributeValueEquals | this_AttributeValueNotEquals_1= ruleAttributeValueNotEquals ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1296:1: (this_AttributeValueEquals_0= ruleAttributeValueEquals | this_AttributeValueNotEquals_1= ruleAttributeValueNotEquals )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1045:1: (this_AttributeValueEquals_0= ruleAttributeValueEquals | this_AttributeValueNotEquals_1= ruleAttributeValueNotEquals )
            int alt12=2;
            int LA12_0 = input.LA(1);
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1296:1: (this_AttributeValueEquals_0= ruleAttributeValueEquals | this_AttributeValueNotEquals_1= ruleAttributeValueNotEquals )
            int alt15=2;
            int LA15_0 = input.LA(1);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

<<<<<<< feature/ecl-support
            if ( (LA12_0==RULE_EQUAL) ) {
                alt12=1;
=======
            if ( (LA15_0==RULE_EQUAL) ) {
                alt15=1;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            }
<<<<<<< feature/ecl-support
            else if ( (LA12_0==RULE_NOT_EQUAL) ) {
                alt12=2;
=======
            else if ( (LA15_0==RULE_NOT_EQUAL) ) {
                alt15=2;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            }
            else {
                NoViableAltException nvae =
<<<<<<< feature/ecl-support
                    new NoViableAltException("", 12, 0, input);
=======
                    new NoViableAltException("", 15, 0, input);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                throw nvae;
            }
<<<<<<< feature/ecl-support
            switch (alt12) {
=======
            switch (alt15) {
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                case 1 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1046:5: this_AttributeValueEquals_0= ruleAttributeValueEquals
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1297:5: this_AttributeValueEquals_0= ruleAttributeValueEquals
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
                     
                            newCompositeNode(grammarAccess.getComparisonAccess().getAttributeValueEqualsParserRuleCall_0()); 
                        
<<<<<<< feature/ecl-support
                    pushFollow(FOLLOW_ruleAttributeValueEquals_in_ruleComparison2435);
=======
                    pushFollow(FOLLOW_ruleAttributeValueEquals_in_ruleComparison3001);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    this_AttributeValueEquals_0=ruleAttributeValueEquals();

                    state._fsp--;

                     
                            current = this_AttributeValueEquals_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1056:5: this_AttributeValueNotEquals_1= ruleAttributeValueNotEquals
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1307:5: this_AttributeValueNotEquals_1= ruleAttributeValueNotEquals
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
                     
                            newCompositeNode(grammarAccess.getComparisonAccess().getAttributeValueNotEqualsParserRuleCall_1()); 
                        
<<<<<<< feature/ecl-support
                    pushFollow(FOLLOW_ruleAttributeValueNotEquals_in_ruleComparison2462);
=======
                    pushFollow(FOLLOW_ruleAttributeValueNotEquals_in_ruleComparison3028);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    this_AttributeValueNotEquals_1=ruleAttributeValueNotEquals();

                    state._fsp--;

                     
                            current = this_AttributeValueNotEquals_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleComparison"


    // $ANTLR start "entryRuleAttributeValueEquals"
<<<<<<< feature/ecl-support
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1072:1: entryRuleAttributeValueEquals returns [EObject current=null] : iv_ruleAttributeValueEquals= ruleAttributeValueEquals EOF ;
=======
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1323:1: entryRuleAttributeValueEquals returns [EObject current=null] : iv_ruleAttributeValueEquals= ruleAttributeValueEquals EOF ;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public final EObject entryRuleAttributeValueEquals() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAttributeValueEquals = null;


        try {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1073:2: (iv_ruleAttributeValueEquals= ruleAttributeValueEquals EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1074:2: iv_ruleAttributeValueEquals= ruleAttributeValueEquals EOF
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1324:2: (iv_ruleAttributeValueEquals= ruleAttributeValueEquals EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1325:2: iv_ruleAttributeValueEquals= ruleAttributeValueEquals EOF
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
             newCompositeNode(grammarAccess.getAttributeValueEqualsRule()); 
<<<<<<< feature/ecl-support
            pushFollow(FOLLOW_ruleAttributeValueEquals_in_entryRuleAttributeValueEquals2497);
=======
            pushFollow(FOLLOW_ruleAttributeValueEquals_in_entryRuleAttributeValueEquals3063);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            iv_ruleAttributeValueEquals=ruleAttributeValueEquals();

            state._fsp--;

             current =iv_ruleAttributeValueEquals; 
<<<<<<< feature/ecl-support
            match(input,EOF,FOLLOW_EOF_in_entryRuleAttributeValueEquals2507); 
=======
            match(input,EOF,FOLLOW_EOF_in_entryRuleAttributeValueEquals3073); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAttributeValueEquals"


    // $ANTLR start "ruleAttributeValueEquals"
<<<<<<< feature/ecl-support
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1081:1: ruleAttributeValueEquals returns [EObject current=null] : (this_EQUAL_0= RULE_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) ) ;
=======
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1332:1: ruleAttributeValueEquals returns [EObject current=null] : (this_EQUAL_0= RULE_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) ) ;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public final EObject ruleAttributeValueEquals() throws RecognitionException {
        EObject current = null;

        Token this_EQUAL_0=null;
        EObject lv_constraint_1_0 = null;


         enterRule(); 
            
        try {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1084:28: ( (this_EQUAL_0= RULE_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1085:1: (this_EQUAL_0= RULE_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1335:28: ( (this_EQUAL_0= RULE_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1336:1: (this_EQUAL_0= RULE_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1085:1: (this_EQUAL_0= RULE_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1085:2: this_EQUAL_0= RULE_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1336:1: (this_EQUAL_0= RULE_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1336:2: this_EQUAL_0= RULE_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            this_EQUAL_0=(Token)match(input,RULE_EQUAL,FOLLOW_RULE_EQUAL_in_ruleAttributeValueEquals2543); 
=======
            this_EQUAL_0=(Token)match(input,RULE_EQUAL,FOLLOW_RULE_EQUAL_in_ruleAttributeValueEquals3109); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
             
                newLeafNode(this_EQUAL_0, grammarAccess.getAttributeValueEqualsAccess().getEQUALTerminalRuleCall_0()); 
                
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1089:1: ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1090:1: (lv_constraint_1_0= ruleSimpleExpressionConstraint )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1340:1: ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1341:1: (lv_constraint_1_0= ruleSimpleExpressionConstraint )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1090:1: (lv_constraint_1_0= ruleSimpleExpressionConstraint )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1091:3: lv_constraint_1_0= ruleSimpleExpressionConstraint
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1341:1: (lv_constraint_1_0= ruleSimpleExpressionConstraint )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1342:3: lv_constraint_1_0= ruleSimpleExpressionConstraint
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
             
            	        newCompositeNode(grammarAccess.getAttributeValueEqualsAccess().getConstraintSimpleExpressionConstraintParserRuleCall_1_0()); 
            	    
<<<<<<< feature/ecl-support
            pushFollow(FOLLOW_ruleSimpleExpressionConstraint_in_ruleAttributeValueEquals2563);
=======
            pushFollow(FOLLOW_ruleSimpleExpressionConstraint_in_ruleAttributeValueEquals3129);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            lv_constraint_1_0=ruleSimpleExpressionConstraint();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getAttributeValueEqualsRule());
            	        }
                   		set(
                   			current, 
                   			"constraint",
                    		lv_constraint_1_0, 
                    		"SimpleExpressionConstraint");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAttributeValueEquals"


    // $ANTLR start "entryRuleAttributeValueNotEquals"
<<<<<<< feature/ecl-support
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1115:1: entryRuleAttributeValueNotEquals returns [EObject current=null] : iv_ruleAttributeValueNotEquals= ruleAttributeValueNotEquals EOF ;
=======
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1366:1: entryRuleAttributeValueNotEquals returns [EObject current=null] : iv_ruleAttributeValueNotEquals= ruleAttributeValueNotEquals EOF ;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public final EObject entryRuleAttributeValueNotEquals() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAttributeValueNotEquals = null;


        try {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1116:2: (iv_ruleAttributeValueNotEquals= ruleAttributeValueNotEquals EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1117:2: iv_ruleAttributeValueNotEquals= ruleAttributeValueNotEquals EOF
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1367:2: (iv_ruleAttributeValueNotEquals= ruleAttributeValueNotEquals EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1368:2: iv_ruleAttributeValueNotEquals= ruleAttributeValueNotEquals EOF
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
             newCompositeNode(grammarAccess.getAttributeValueNotEqualsRule()); 
<<<<<<< feature/ecl-support
            pushFollow(FOLLOW_ruleAttributeValueNotEquals_in_entryRuleAttributeValueNotEquals2599);
=======
            pushFollow(FOLLOW_ruleAttributeValueNotEquals_in_entryRuleAttributeValueNotEquals3165);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            iv_ruleAttributeValueNotEquals=ruleAttributeValueNotEquals();

            state._fsp--;

             current =iv_ruleAttributeValueNotEquals; 
<<<<<<< feature/ecl-support
            match(input,EOF,FOLLOW_EOF_in_entryRuleAttributeValueNotEquals2609); 
=======
            match(input,EOF,FOLLOW_EOF_in_entryRuleAttributeValueNotEquals3175); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAttributeValueNotEquals"


    // $ANTLR start "ruleAttributeValueNotEquals"
<<<<<<< feature/ecl-support
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1124:1: ruleAttributeValueNotEquals returns [EObject current=null] : (this_NOT_EQUAL_0= RULE_NOT_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) ) ;
=======
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1375:1: ruleAttributeValueNotEquals returns [EObject current=null] : (this_NOT_EQUAL_0= RULE_NOT_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) ) ;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public final EObject ruleAttributeValueNotEquals() throws RecognitionException {
        EObject current = null;

        Token this_NOT_EQUAL_0=null;
        EObject lv_constraint_1_0 = null;


         enterRule(); 
            
        try {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1127:28: ( (this_NOT_EQUAL_0= RULE_NOT_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1128:1: (this_NOT_EQUAL_0= RULE_NOT_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1378:28: ( (this_NOT_EQUAL_0= RULE_NOT_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1379:1: (this_NOT_EQUAL_0= RULE_NOT_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1128:1: (this_NOT_EQUAL_0= RULE_NOT_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1128:2: this_NOT_EQUAL_0= RULE_NOT_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1379:1: (this_NOT_EQUAL_0= RULE_NOT_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1379:2: this_NOT_EQUAL_0= RULE_NOT_EQUAL ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            this_NOT_EQUAL_0=(Token)match(input,RULE_NOT_EQUAL,FOLLOW_RULE_NOT_EQUAL_in_ruleAttributeValueNotEquals2645); 
=======
            this_NOT_EQUAL_0=(Token)match(input,RULE_NOT_EQUAL,FOLLOW_RULE_NOT_EQUAL_in_ruleAttributeValueNotEquals3211); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
             
                newLeafNode(this_NOT_EQUAL_0, grammarAccess.getAttributeValueNotEqualsAccess().getNOT_EQUALTerminalRuleCall_0()); 
                
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1132:1: ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1133:1: (lv_constraint_1_0= ruleSimpleExpressionConstraint )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1383:1: ( (lv_constraint_1_0= ruleSimpleExpressionConstraint ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1384:1: (lv_constraint_1_0= ruleSimpleExpressionConstraint )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1133:1: (lv_constraint_1_0= ruleSimpleExpressionConstraint )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1134:3: lv_constraint_1_0= ruleSimpleExpressionConstraint
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1384:1: (lv_constraint_1_0= ruleSimpleExpressionConstraint )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1385:3: lv_constraint_1_0= ruleSimpleExpressionConstraint
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
             
            	        newCompositeNode(grammarAccess.getAttributeValueNotEqualsAccess().getConstraintSimpleExpressionConstraintParserRuleCall_1_0()); 
            	    
<<<<<<< feature/ecl-support
            pushFollow(FOLLOW_ruleSimpleExpressionConstraint_in_ruleAttributeValueNotEquals2665);
=======
            pushFollow(FOLLOW_ruleSimpleExpressionConstraint_in_ruleAttributeValueNotEquals3231);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            lv_constraint_1_0=ruleSimpleExpressionConstraint();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getAttributeValueNotEqualsRule());
            	        }
                   		set(
                   			current, 
                   			"constraint",
                    		lv_constraint_1_0, 
                    		"SimpleExpressionConstraint");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAttributeValueNotEquals"


    // $ANTLR start "entryRuleNestedExpression"
<<<<<<< feature/ecl-support
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1158:1: entryRuleNestedExpression returns [EObject current=null] : iv_ruleNestedExpression= ruleNestedExpression EOF ;
=======
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1409:1: entryRuleNestedExpression returns [EObject current=null] : iv_ruleNestedExpression= ruleNestedExpression EOF ;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public final EObject entryRuleNestedExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleNestedExpression = null;


        try {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1159:2: (iv_ruleNestedExpression= ruleNestedExpression EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1160:2: iv_ruleNestedExpression= ruleNestedExpression EOF
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1410:2: (iv_ruleNestedExpression= ruleNestedExpression EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1411:2: iv_ruleNestedExpression= ruleNestedExpression EOF
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
             newCompositeNode(grammarAccess.getNestedExpressionRule()); 
<<<<<<< feature/ecl-support
            pushFollow(FOLLOW_ruleNestedExpression_in_entryRuleNestedExpression2701);
=======
            pushFollow(FOLLOW_ruleNestedExpression_in_entryRuleNestedExpression3267);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            iv_ruleNestedExpression=ruleNestedExpression();

            state._fsp--;

             current =iv_ruleNestedExpression; 
<<<<<<< feature/ecl-support
            match(input,EOF,FOLLOW_EOF_in_entryRuleNestedExpression2711); 
=======
            match(input,EOF,FOLLOW_EOF_in_entryRuleNestedExpression3277); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleNestedExpression"


    // $ANTLR start "ruleNestedExpression"
<<<<<<< feature/ecl-support
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1167:1: ruleNestedExpression returns [EObject current=null] : (this_ROUND_OPEN_0= RULE_ROUND_OPEN ( (lv_nested_1_0= ruleExpressionConstraint ) ) this_ROUND_CLOSE_2= RULE_ROUND_CLOSE ) ;
=======
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1418:1: ruleNestedExpression returns [EObject current=null] : (this_ROUND_OPEN_0= RULE_ROUND_OPEN ( (lv_nested_1_0= ruleExpressionConstraint ) ) this_ROUND_CLOSE_2= RULE_ROUND_CLOSE ) ;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public final EObject ruleNestedExpression() throws RecognitionException {
        EObject current = null;

        Token this_ROUND_OPEN_0=null;
        Token this_ROUND_CLOSE_2=null;
        EObject lv_nested_1_0 = null;


         enterRule(); 
            
        try {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1170:28: ( (this_ROUND_OPEN_0= RULE_ROUND_OPEN ( (lv_nested_1_0= ruleExpressionConstraint ) ) this_ROUND_CLOSE_2= RULE_ROUND_CLOSE ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1171:1: (this_ROUND_OPEN_0= RULE_ROUND_OPEN ( (lv_nested_1_0= ruleExpressionConstraint ) ) this_ROUND_CLOSE_2= RULE_ROUND_CLOSE )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1421:28: ( (this_ROUND_OPEN_0= RULE_ROUND_OPEN ( (lv_nested_1_0= ruleExpressionConstraint ) ) this_ROUND_CLOSE_2= RULE_ROUND_CLOSE ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1422:1: (this_ROUND_OPEN_0= RULE_ROUND_OPEN ( (lv_nested_1_0= ruleExpressionConstraint ) ) this_ROUND_CLOSE_2= RULE_ROUND_CLOSE )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1171:1: (this_ROUND_OPEN_0= RULE_ROUND_OPEN ( (lv_nested_1_0= ruleExpressionConstraint ) ) this_ROUND_CLOSE_2= RULE_ROUND_CLOSE )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1171:2: this_ROUND_OPEN_0= RULE_ROUND_OPEN ( (lv_nested_1_0= ruleExpressionConstraint ) ) this_ROUND_CLOSE_2= RULE_ROUND_CLOSE
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1422:1: (this_ROUND_OPEN_0= RULE_ROUND_OPEN ( (lv_nested_1_0= ruleExpressionConstraint ) ) this_ROUND_CLOSE_2= RULE_ROUND_CLOSE )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1422:2: this_ROUND_OPEN_0= RULE_ROUND_OPEN ( (lv_nested_1_0= ruleExpressionConstraint ) ) this_ROUND_CLOSE_2= RULE_ROUND_CLOSE
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            this_ROUND_OPEN_0=(Token)match(input,RULE_ROUND_OPEN,FOLLOW_RULE_ROUND_OPEN_in_ruleNestedExpression2747); 
=======
            this_ROUND_OPEN_0=(Token)match(input,RULE_ROUND_OPEN,FOLLOW_RULE_ROUND_OPEN_in_ruleNestedExpression3313); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
             
                newLeafNode(this_ROUND_OPEN_0, grammarAccess.getNestedExpressionAccess().getROUND_OPENTerminalRuleCall_0()); 
                
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1175:1: ( (lv_nested_1_0= ruleExpressionConstraint ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1176:1: (lv_nested_1_0= ruleExpressionConstraint )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1426:1: ( (lv_nested_1_0= ruleExpressionConstraint ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1427:1: (lv_nested_1_0= ruleExpressionConstraint )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1176:1: (lv_nested_1_0= ruleExpressionConstraint )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1177:3: lv_nested_1_0= ruleExpressionConstraint
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1427:1: (lv_nested_1_0= ruleExpressionConstraint )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1428:3: lv_nested_1_0= ruleExpressionConstraint
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
             
            	        newCompositeNode(grammarAccess.getNestedExpressionAccess().getNestedExpressionConstraintParserRuleCall_1_0()); 
            	    
<<<<<<< feature/ecl-support
            pushFollow(FOLLOW_ruleExpressionConstraint_in_ruleNestedExpression2767);
=======
            pushFollow(FOLLOW_ruleExpressionConstraint_in_ruleNestedExpression3333);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            lv_nested_1_0=ruleExpressionConstraint();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getNestedExpressionRule());
            	        }
                   		set(
                   			current, 
                   			"nested",
                    		lv_nested_1_0, 
                    		"ExpressionConstraint");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

<<<<<<< feature/ecl-support
            this_ROUND_CLOSE_2=(Token)match(input,RULE_ROUND_CLOSE,FOLLOW_RULE_ROUND_CLOSE_in_ruleNestedExpression2778); 
=======
            this_ROUND_CLOSE_2=(Token)match(input,RULE_ROUND_CLOSE,FOLLOW_RULE_ROUND_CLOSE_in_ruleNestedExpression3344); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
             
                newLeafNode(this_ROUND_CLOSE_2, grammarAccess.getNestedExpressionAccess().getROUND_CLOSETerminalRuleCall_2()); 
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleNestedExpression"


    // $ANTLR start "entryRuleSnomedIdentifier"
<<<<<<< feature/ecl-support
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1205:1: entryRuleSnomedIdentifier returns [String current=null] : iv_ruleSnomedIdentifier= ruleSnomedIdentifier EOF ;
=======
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1456:1: entryRuleSnomedIdentifier returns [String current=null] : iv_ruleSnomedIdentifier= ruleSnomedIdentifier EOF ;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public final String entryRuleSnomedIdentifier() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleSnomedIdentifier = null;


         
        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();
        	
        try {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1209:2: (iv_ruleSnomedIdentifier= ruleSnomedIdentifier EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1210:2: iv_ruleSnomedIdentifier= ruleSnomedIdentifier EOF
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1460:2: (iv_ruleSnomedIdentifier= ruleSnomedIdentifier EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1461:2: iv_ruleSnomedIdentifier= ruleSnomedIdentifier EOF
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
             newCompositeNode(grammarAccess.getSnomedIdentifierRule()); 
<<<<<<< feature/ecl-support
            pushFollow(FOLLOW_ruleSnomedIdentifier_in_entryRuleSnomedIdentifier2820);
=======
            pushFollow(FOLLOW_ruleSnomedIdentifier_in_entryRuleSnomedIdentifier3386);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            iv_ruleSnomedIdentifier=ruleSnomedIdentifier();

            state._fsp--;

             current =iv_ruleSnomedIdentifier.getText(); 
<<<<<<< feature/ecl-support
            match(input,EOF,FOLLOW_EOF_in_entryRuleSnomedIdentifier2831); 
=======
            match(input,EOF,FOLLOW_EOF_in_entryRuleSnomedIdentifier3397); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "entryRuleSnomedIdentifier"


    // $ANTLR start "ruleSnomedIdentifier"
<<<<<<< feature/ecl-support
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1220:1: ruleSnomedIdentifier returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_DIGIT_NONZERO_0= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO | this_ZERO_2= RULE_ZERO ) (this_DIGIT_NONZERO_3= RULE_DIGIT_NONZERO | this_ZERO_4= RULE_ZERO ) (this_DIGIT_NONZERO_5= RULE_DIGIT_NONZERO | this_ZERO_6= RULE_ZERO ) (this_DIGIT_NONZERO_7= RULE_DIGIT_NONZERO | this_ZERO_8= RULE_ZERO ) (this_DIGIT_NONZERO_9= RULE_DIGIT_NONZERO | this_ZERO_10= RULE_ZERO )+ ) ;
=======
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1471:1: ruleSnomedIdentifier returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_DIGIT_NONZERO_0= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO | this_ZERO_2= RULE_ZERO ) (this_DIGIT_NONZERO_3= RULE_DIGIT_NONZERO | this_ZERO_4= RULE_ZERO ) (this_DIGIT_NONZERO_5= RULE_DIGIT_NONZERO | this_ZERO_6= RULE_ZERO ) (this_DIGIT_NONZERO_7= RULE_DIGIT_NONZERO | this_ZERO_8= RULE_ZERO ) (this_DIGIT_NONZERO_9= RULE_DIGIT_NONZERO | this_ZERO_10= RULE_ZERO )+ ) ;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public final AntlrDatatypeRuleToken ruleSnomedIdentifier() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_DIGIT_NONZERO_0=null;
        Token this_DIGIT_NONZERO_1=null;
        Token this_ZERO_2=null;
        Token this_DIGIT_NONZERO_3=null;
        Token this_ZERO_4=null;
        Token this_DIGIT_NONZERO_5=null;
        Token this_ZERO_6=null;
        Token this_DIGIT_NONZERO_7=null;
        Token this_ZERO_8=null;
        Token this_DIGIT_NONZERO_9=null;
        Token this_ZERO_10=null;

         enterRule(); 
        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();
            
        try {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1224:28: ( (this_DIGIT_NONZERO_0= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO | this_ZERO_2= RULE_ZERO ) (this_DIGIT_NONZERO_3= RULE_DIGIT_NONZERO | this_ZERO_4= RULE_ZERO ) (this_DIGIT_NONZERO_5= RULE_DIGIT_NONZERO | this_ZERO_6= RULE_ZERO ) (this_DIGIT_NONZERO_7= RULE_DIGIT_NONZERO | this_ZERO_8= RULE_ZERO ) (this_DIGIT_NONZERO_9= RULE_DIGIT_NONZERO | this_ZERO_10= RULE_ZERO )+ ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1225:1: (this_DIGIT_NONZERO_0= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO | this_ZERO_2= RULE_ZERO ) (this_DIGIT_NONZERO_3= RULE_DIGIT_NONZERO | this_ZERO_4= RULE_ZERO ) (this_DIGIT_NONZERO_5= RULE_DIGIT_NONZERO | this_ZERO_6= RULE_ZERO ) (this_DIGIT_NONZERO_7= RULE_DIGIT_NONZERO | this_ZERO_8= RULE_ZERO ) (this_DIGIT_NONZERO_9= RULE_DIGIT_NONZERO | this_ZERO_10= RULE_ZERO )+ )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1475:28: ( (this_DIGIT_NONZERO_0= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO | this_ZERO_2= RULE_ZERO ) (this_DIGIT_NONZERO_3= RULE_DIGIT_NONZERO | this_ZERO_4= RULE_ZERO ) (this_DIGIT_NONZERO_5= RULE_DIGIT_NONZERO | this_ZERO_6= RULE_ZERO ) (this_DIGIT_NONZERO_7= RULE_DIGIT_NONZERO | this_ZERO_8= RULE_ZERO ) (this_DIGIT_NONZERO_9= RULE_DIGIT_NONZERO | this_ZERO_10= RULE_ZERO )+ ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1476:1: (this_DIGIT_NONZERO_0= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO | this_ZERO_2= RULE_ZERO ) (this_DIGIT_NONZERO_3= RULE_DIGIT_NONZERO | this_ZERO_4= RULE_ZERO ) (this_DIGIT_NONZERO_5= RULE_DIGIT_NONZERO | this_ZERO_6= RULE_ZERO ) (this_DIGIT_NONZERO_7= RULE_DIGIT_NONZERO | this_ZERO_8= RULE_ZERO ) (this_DIGIT_NONZERO_9= RULE_DIGIT_NONZERO | this_ZERO_10= RULE_ZERO )+ )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1225:1: (this_DIGIT_NONZERO_0= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO | this_ZERO_2= RULE_ZERO ) (this_DIGIT_NONZERO_3= RULE_DIGIT_NONZERO | this_ZERO_4= RULE_ZERO ) (this_DIGIT_NONZERO_5= RULE_DIGIT_NONZERO | this_ZERO_6= RULE_ZERO ) (this_DIGIT_NONZERO_7= RULE_DIGIT_NONZERO | this_ZERO_8= RULE_ZERO ) (this_DIGIT_NONZERO_9= RULE_DIGIT_NONZERO | this_ZERO_10= RULE_ZERO )+ )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1225:6: this_DIGIT_NONZERO_0= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO | this_ZERO_2= RULE_ZERO ) (this_DIGIT_NONZERO_3= RULE_DIGIT_NONZERO | this_ZERO_4= RULE_ZERO ) (this_DIGIT_NONZERO_5= RULE_DIGIT_NONZERO | this_ZERO_6= RULE_ZERO ) (this_DIGIT_NONZERO_7= RULE_DIGIT_NONZERO | this_ZERO_8= RULE_ZERO ) (this_DIGIT_NONZERO_9= RULE_DIGIT_NONZERO | this_ZERO_10= RULE_ZERO )+
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1476:1: (this_DIGIT_NONZERO_0= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO | this_ZERO_2= RULE_ZERO ) (this_DIGIT_NONZERO_3= RULE_DIGIT_NONZERO | this_ZERO_4= RULE_ZERO ) (this_DIGIT_NONZERO_5= RULE_DIGIT_NONZERO | this_ZERO_6= RULE_ZERO ) (this_DIGIT_NONZERO_7= RULE_DIGIT_NONZERO | this_ZERO_8= RULE_ZERO ) (this_DIGIT_NONZERO_9= RULE_DIGIT_NONZERO | this_ZERO_10= RULE_ZERO )+ )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1476:6: this_DIGIT_NONZERO_0= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO | this_ZERO_2= RULE_ZERO ) (this_DIGIT_NONZERO_3= RULE_DIGIT_NONZERO | this_ZERO_4= RULE_ZERO ) (this_DIGIT_NONZERO_5= RULE_DIGIT_NONZERO | this_ZERO_6= RULE_ZERO ) (this_DIGIT_NONZERO_7= RULE_DIGIT_NONZERO | this_ZERO_8= RULE_ZERO ) (this_DIGIT_NONZERO_9= RULE_DIGIT_NONZERO | this_ZERO_10= RULE_ZERO )+
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            this_DIGIT_NONZERO_0=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier2875); 
=======
            this_DIGIT_NONZERO_0=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3441); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

            		current.merge(this_DIGIT_NONZERO_0);
                
             
                newLeafNode(this_DIGIT_NONZERO_0, grammarAccess.getSnomedIdentifierAccess().getDIGIT_NONZEROTerminalRuleCall_0()); 
                
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1232:1: (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO | this_ZERO_2= RULE_ZERO )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==RULE_DIGIT_NONZERO) ) {
                alt13=1;
            }
            else if ( (LA13_0==RULE_ZERO) ) {
                alt13=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1232:6: this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO
                    {
                    this_DIGIT_NONZERO_1=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier2896); 

                    		current.merge(this_DIGIT_NONZERO_1);
                        
                     
                        newLeafNode(this_DIGIT_NONZERO_1, grammarAccess.getSnomedIdentifierAccess().getDIGIT_NONZEROTerminalRuleCall_1_0()); 
                        

                    }
                    break;
                case 2 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1240:10: this_ZERO_2= RULE_ZERO
                    {
                    this_ZERO_2=(Token)match(input,RULE_ZERO,FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier2922); 

                    		current.merge(this_ZERO_2);
                        
                     
                        newLeafNode(this_ZERO_2, grammarAccess.getSnomedIdentifierAccess().getZEROTerminalRuleCall_1_1()); 
                        

                    }
                    break;

            }

            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1247:2: (this_DIGIT_NONZERO_3= RULE_DIGIT_NONZERO | this_ZERO_4= RULE_ZERO )
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==RULE_DIGIT_NONZERO) ) {
                alt14=1;
            }
            else if ( (LA14_0==RULE_ZERO) ) {
                alt14=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;
            }
            switch (alt14) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1247:7: this_DIGIT_NONZERO_3= RULE_DIGIT_NONZERO
                    {
                    this_DIGIT_NONZERO_3=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier2944); 

                    		current.merge(this_DIGIT_NONZERO_3);
                        
                     
                        newLeafNode(this_DIGIT_NONZERO_3, grammarAccess.getSnomedIdentifierAccess().getDIGIT_NONZEROTerminalRuleCall_2_0()); 
                        

                    }
                    break;
                case 2 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1255:10: this_ZERO_4= RULE_ZERO
                    {
                    this_ZERO_4=(Token)match(input,RULE_ZERO,FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier2970); 

                    		current.merge(this_ZERO_4);
                        
                     
                        newLeafNode(this_ZERO_4, grammarAccess.getSnomedIdentifierAccess().getZEROTerminalRuleCall_2_1()); 
                        

                    }
                    break;

            }

            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1262:2: (this_DIGIT_NONZERO_5= RULE_DIGIT_NONZERO | this_ZERO_6= RULE_ZERO )
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==RULE_DIGIT_NONZERO) ) {
                alt15=1;
            }
            else if ( (LA15_0==RULE_ZERO) ) {
                alt15=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }
            switch (alt15) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1262:7: this_DIGIT_NONZERO_5= RULE_DIGIT_NONZERO
                    {
                    this_DIGIT_NONZERO_5=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier2992); 

                    		current.merge(this_DIGIT_NONZERO_5);
                        
                     
                        newLeafNode(this_DIGIT_NONZERO_5, grammarAccess.getSnomedIdentifierAccess().getDIGIT_NONZEROTerminalRuleCall_3_0()); 
                        

                    }
                    break;
                case 2 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1270:10: this_ZERO_6= RULE_ZERO
                    {
                    this_ZERO_6=(Token)match(input,RULE_ZERO,FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3018); 

                    		current.merge(this_ZERO_6);
                        
                     
                        newLeafNode(this_ZERO_6, grammarAccess.getSnomedIdentifierAccess().getZEROTerminalRuleCall_3_1()); 
                        

                    }
                    break;

            }

            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1277:2: (this_DIGIT_NONZERO_7= RULE_DIGIT_NONZERO | this_ZERO_8= RULE_ZERO )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1483:1: (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO | this_ZERO_2= RULE_ZERO )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==RULE_DIGIT_NONZERO) ) {
                alt16=1;
            }
            else if ( (LA16_0==RULE_ZERO) ) {
                alt16=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1277:7: this_DIGIT_NONZERO_7= RULE_DIGIT_NONZERO
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1483:6: this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_DIGIT_NONZERO_7=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3040); 
=======
                    this_DIGIT_NONZERO_1=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3462); 

                    		current.merge(this_DIGIT_NONZERO_1);
                        
                     
                        newLeafNode(this_DIGIT_NONZERO_1, grammarAccess.getSnomedIdentifierAccess().getDIGIT_NONZEROTerminalRuleCall_1_0()); 
                        

                    }
                    break;
                case 2 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1491:10: this_ZERO_2= RULE_ZERO
                    {
                    this_ZERO_2=(Token)match(input,RULE_ZERO,FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3488); 

                    		current.merge(this_ZERO_2);
                        
                     
                        newLeafNode(this_ZERO_2, grammarAccess.getSnomedIdentifierAccess().getZEROTerminalRuleCall_1_1()); 
                        

                    }
                    break;

            }

            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1498:2: (this_DIGIT_NONZERO_3= RULE_DIGIT_NONZERO | this_ZERO_4= RULE_ZERO )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==RULE_DIGIT_NONZERO) ) {
                alt17=1;
            }
            else if ( (LA17_0==RULE_ZERO) ) {
                alt17=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1498:7: this_DIGIT_NONZERO_3= RULE_DIGIT_NONZERO
                    {
                    this_DIGIT_NONZERO_3=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3510); 

                    		current.merge(this_DIGIT_NONZERO_3);
                        
                     
                        newLeafNode(this_DIGIT_NONZERO_3, grammarAccess.getSnomedIdentifierAccess().getDIGIT_NONZEROTerminalRuleCall_2_0()); 
                        

                    }
                    break;
                case 2 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1506:10: this_ZERO_4= RULE_ZERO
                    {
                    this_ZERO_4=(Token)match(input,RULE_ZERO,FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3536); 

                    		current.merge(this_ZERO_4);
                        
                     
                        newLeafNode(this_ZERO_4, grammarAccess.getSnomedIdentifierAccess().getZEROTerminalRuleCall_2_1()); 
                        

                    }
                    break;

            }

            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1513:2: (this_DIGIT_NONZERO_5= RULE_DIGIT_NONZERO | this_ZERO_6= RULE_ZERO )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==RULE_DIGIT_NONZERO) ) {
                alt18=1;
            }
            else if ( (LA18_0==RULE_ZERO) ) {
                alt18=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }
            switch (alt18) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1513:7: this_DIGIT_NONZERO_5= RULE_DIGIT_NONZERO
                    {
                    this_DIGIT_NONZERO_5=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3558); 

                    		current.merge(this_DIGIT_NONZERO_5);
                        
                     
                        newLeafNode(this_DIGIT_NONZERO_5, grammarAccess.getSnomedIdentifierAccess().getDIGIT_NONZEROTerminalRuleCall_3_0()); 
                        

                    }
                    break;
                case 2 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1521:10: this_ZERO_6= RULE_ZERO
                    {
                    this_ZERO_6=(Token)match(input,RULE_ZERO,FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3584); 

                    		current.merge(this_ZERO_6);
                        
                     
                        newLeafNode(this_ZERO_6, grammarAccess.getSnomedIdentifierAccess().getZEROTerminalRuleCall_3_1()); 
                        

                    }
                    break;

            }

            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1528:2: (this_DIGIT_NONZERO_7= RULE_DIGIT_NONZERO | this_ZERO_8= RULE_ZERO )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==RULE_DIGIT_NONZERO) ) {
                alt19=1;
            }
            else if ( (LA19_0==RULE_ZERO) ) {
                alt19=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1528:7: this_DIGIT_NONZERO_7= RULE_DIGIT_NONZERO
                    {
                    this_DIGIT_NONZERO_7=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3606); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_DIGIT_NONZERO_7);
                        
                     
                        newLeafNode(this_DIGIT_NONZERO_7, grammarAccess.getSnomedIdentifierAccess().getDIGIT_NONZEROTerminalRuleCall_4_0()); 
                        

                    }
                    break;
                case 2 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1285:10: this_ZERO_8= RULE_ZERO
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1536:10: this_ZERO_8= RULE_ZERO
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_ZERO_8=(Token)match(input,RULE_ZERO,FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3066); 
=======
                    this_ZERO_8=(Token)match(input,RULE_ZERO,FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3632); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_ZERO_8);
                        
                     
                        newLeafNode(this_ZERO_8, grammarAccess.getSnomedIdentifierAccess().getZEROTerminalRuleCall_4_1()); 
                        

                    }
                    break;

            }

<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1292:2: (this_DIGIT_NONZERO_9= RULE_DIGIT_NONZERO | this_ZERO_10= RULE_ZERO )+
            int cnt17=0;
            loop17:
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1543:2: (this_DIGIT_NONZERO_9= RULE_DIGIT_NONZERO | this_ZERO_10= RULE_ZERO )+
            int cnt20=0;
            loop20:
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            do {
<<<<<<< feature/ecl-support
                int alt17=3;
                int LA17_0 = input.LA(1);
=======
                int alt20=3;
                int LA20_0 = input.LA(1);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

<<<<<<< feature/ecl-support
                if ( (LA17_0==RULE_DIGIT_NONZERO) ) {
                    alt17=1;
=======
                if ( (LA20_0==RULE_DIGIT_NONZERO) ) {
                    alt20=1;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
<<<<<<< feature/ecl-support
                else if ( (LA17_0==RULE_ZERO) ) {
                    alt17=2;
=======
                else if ( (LA20_0==RULE_ZERO) ) {
                    alt20=2;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }


<<<<<<< feature/ecl-support
                switch (alt17) {
=======
                switch (alt20) {
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	case 1 :
<<<<<<< feature/ecl-support
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1292:7: this_DIGIT_NONZERO_9= RULE_DIGIT_NONZERO
=======
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1543:7: this_DIGIT_NONZERO_9= RULE_DIGIT_NONZERO
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	    {
<<<<<<< feature/ecl-support
            	    this_DIGIT_NONZERO_9=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3088); 
=======
            	    this_DIGIT_NONZERO_9=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3654); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

            	    		current.merge(this_DIGIT_NONZERO_9);
            	        
            	     
            	        newLeafNode(this_DIGIT_NONZERO_9, grammarAccess.getSnomedIdentifierAccess().getDIGIT_NONZEROTerminalRuleCall_5_0()); 
            	        

            	    }
            	    break;
            	case 2 :
<<<<<<< feature/ecl-support
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1300:10: this_ZERO_10= RULE_ZERO
=======
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1551:10: this_ZERO_10= RULE_ZERO
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	    {
<<<<<<< feature/ecl-support
            	    this_ZERO_10=(Token)match(input,RULE_ZERO,FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3114); 
=======
            	    this_ZERO_10=(Token)match(input,RULE_ZERO,FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3680); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

            	    		current.merge(this_ZERO_10);
            	        
            	     
            	        newLeafNode(this_ZERO_10, grammarAccess.getSnomedIdentifierAccess().getZEROTerminalRuleCall_5_1()); 
            	        

            	    }
            	    break;

            	default :
<<<<<<< feature/ecl-support
            	    if ( cnt17 >= 1 ) break loop17;
=======
            	    if ( cnt20 >= 1 ) break loop20;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                        EarlyExitException eee =
<<<<<<< feature/ecl-support
                            new EarlyExitException(17, input);
=======
                            new EarlyExitException(20, input);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                        throw eee;
                }
<<<<<<< feature/ecl-support
                cnt17++;
=======
                cnt20++;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "ruleSnomedIdentifier"


    // $ANTLR start "entryRuleTerm"
<<<<<<< feature/ecl-support
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1318:1: entryRuleTerm returns [String current=null] : iv_ruleTerm= ruleTerm EOF ;
=======
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1569:1: entryRuleTerm returns [String current=null] : iv_ruleTerm= ruleTerm EOF ;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public final String entryRuleTerm() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleTerm = null;


         
        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();
        	
        try {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1322:2: (iv_ruleTerm= ruleTerm EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1323:2: iv_ruleTerm= ruleTerm EOF
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1573:2: (iv_ruleTerm= ruleTerm EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1574:2: iv_ruleTerm= ruleTerm EOF
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
             newCompositeNode(grammarAccess.getTermRule()); 
<<<<<<< feature/ecl-support
            pushFollow(FOLLOW_ruleTerm_in_entryRuleTerm3172);
=======
            pushFollow(FOLLOW_ruleTerm_in_entryRuleTerm3738);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            iv_ruleTerm=ruleTerm();

            state._fsp--;

             current =iv_ruleTerm.getText(); 
<<<<<<< feature/ecl-support
            match(input,EOF,FOLLOW_EOF_in_entryRuleTerm3183); 
=======
            match(input,EOF,FOLLOW_EOF_in_entryRuleTerm3749); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "entryRuleTerm"


    // $ANTLR start "ruleTerm"
<<<<<<< feature/ecl-support
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1333:1: ruleTerm returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : ( (this_TermCharacter_0= ruleTermCharacter )+ ( (this_WS_1= RULE_WS )+ (this_TermCharacter_2= ruleTermCharacter )+ )* ) ;
=======
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1584:1: ruleTerm returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : ( (this_TermCharacter_0= ruleTermCharacter )+ ( (this_WS_1= RULE_WS )+ (this_TermCharacter_2= ruleTermCharacter )+ )* ) ;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public final AntlrDatatypeRuleToken ruleTerm() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_WS_1=null;
        AntlrDatatypeRuleToken this_TermCharacter_0 = null;

        AntlrDatatypeRuleToken this_TermCharacter_2 = null;


         enterRule(); 
        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();
            
        try {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1337:28: ( ( (this_TermCharacter_0= ruleTermCharacter )+ ( (this_WS_1= RULE_WS )+ (this_TermCharacter_2= ruleTermCharacter )+ )* ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1338:1: ( (this_TermCharacter_0= ruleTermCharacter )+ ( (this_WS_1= RULE_WS )+ (this_TermCharacter_2= ruleTermCharacter )+ )* )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1588:28: ( ( (this_TermCharacter_0= ruleTermCharacter )+ ( (this_WS_1= RULE_WS )+ (this_TermCharacter_2= ruleTermCharacter )+ )* ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1589:1: ( (this_TermCharacter_0= ruleTermCharacter )+ ( (this_WS_1= RULE_WS )+ (this_TermCharacter_2= ruleTermCharacter )+ )* )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1338:1: ( (this_TermCharacter_0= ruleTermCharacter )+ ( (this_WS_1= RULE_WS )+ (this_TermCharacter_2= ruleTermCharacter )+ )* )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1338:2: (this_TermCharacter_0= ruleTermCharacter )+ ( (this_WS_1= RULE_WS )+ (this_TermCharacter_2= ruleTermCharacter )+ )*
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1589:1: ( (this_TermCharacter_0= ruleTermCharacter )+ ( (this_WS_1= RULE_WS )+ (this_TermCharacter_2= ruleTermCharacter )+ )* )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1589:2: (this_TermCharacter_0= ruleTermCharacter )+ ( (this_WS_1= RULE_WS )+ (this_TermCharacter_2= ruleTermCharacter )+ )*
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1338:2: (this_TermCharacter_0= ruleTermCharacter )+
            int cnt18=0;
            loop18:
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1589:2: (this_TermCharacter_0= ruleTermCharacter )+
            int cnt21=0;
            loop21:
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            do {
<<<<<<< feature/ecl-support
                int alt18=2;
                int LA18_0 = input.LA(1);
=======
                int alt21=2;
                int LA21_0 = input.LA(1);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

<<<<<<< feature/ecl-support
                if ( ((LA18_0>=RULE_OR && LA18_0<=RULE_COMMA)||(LA18_0>=RULE_COLON && LA18_0<=RULE_CARET)||(LA18_0>=RULE_REVERSED && LA18_0<=RULE_EQUAL)||(LA18_0>=RULE_ROUND_OPEN && LA18_0<=RULE_ZERO)||(LA18_0>=RULE_NOT && LA18_0<=RULE_OTHER_CHARACTER)) ) {
                    alt18=1;
=======
                if ( ((LA21_0>=RULE_OR && LA21_0<=RULE_CARET)||(LA21_0>=RULE_WILDCARD && LA21_0<=RULE_ZERO)||(LA21_0>=RULE_NOT && LA21_0<=RULE_OTHER_CHARACTER)) ) {
                    alt21=1;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }


<<<<<<< feature/ecl-support
                switch (alt18) {
=======
                switch (alt21) {
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	case 1 :
<<<<<<< feature/ecl-support
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1339:5: this_TermCharacter_0= ruleTermCharacter
=======
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1590:5: this_TermCharacter_0= ruleTermCharacter
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	    {
            	     
            	            newCompositeNode(grammarAccess.getTermAccess().getTermCharacterParserRuleCall_0()); 
            	        
<<<<<<< feature/ecl-support
            	    pushFollow(FOLLOW_ruleTermCharacter_in_ruleTerm3235);
=======
            	    pushFollow(FOLLOW_ruleTermCharacter_in_ruleTerm3801);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	    this_TermCharacter_0=ruleTermCharacter();

            	    state._fsp--;


            	    		current.merge(this_TermCharacter_0);
            	        
            	     
            	            afterParserOrEnumRuleCall();
            	        

            	    }
            	    break;

            	default :
<<<<<<< feature/ecl-support
            	    if ( cnt18 >= 1 ) break loop18;
=======
            	    if ( cnt21 >= 1 ) break loop21;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                        EarlyExitException eee =
<<<<<<< feature/ecl-support
                            new EarlyExitException(18, input);
=======
                            new EarlyExitException(21, input);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                        throw eee;
                }
<<<<<<< feature/ecl-support
                cnt18++;
=======
                cnt21++;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            } while (true);

<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1349:3: ( (this_WS_1= RULE_WS )+ (this_TermCharacter_2= ruleTermCharacter )+ )*
            loop21:
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1600:3: ( (this_WS_1= RULE_WS )+ (this_TermCharacter_2= ruleTermCharacter )+ )*
            loop24:
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            do {
<<<<<<< feature/ecl-support
                int alt21=2;
                int LA21_0 = input.LA(1);
=======
                int alt24=2;
                int LA24_0 = input.LA(1);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

<<<<<<< feature/ecl-support
                if ( (LA21_0==RULE_WS) ) {
                    alt21=1;
=======
                if ( (LA24_0==RULE_WS) ) {
                    alt24=1;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }


<<<<<<< feature/ecl-support
                switch (alt21) {
=======
                switch (alt24) {
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	case 1 :
<<<<<<< feature/ecl-support
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1349:4: (this_WS_1= RULE_WS )+ (this_TermCharacter_2= ruleTermCharacter )+
=======
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1600:4: (this_WS_1= RULE_WS )+ (this_TermCharacter_2= ruleTermCharacter )+
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	    {
<<<<<<< feature/ecl-support
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1349:4: (this_WS_1= RULE_WS )+
            	    int cnt19=0;
            	    loop19:
=======
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1600:4: (this_WS_1= RULE_WS )+
            	    int cnt22=0;
            	    loop22:
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	    do {
<<<<<<< feature/ecl-support
            	        int alt19=2;
            	        int LA19_0 = input.LA(1);
=======
            	        int alt22=2;
            	        int LA22_0 = input.LA(1);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

<<<<<<< feature/ecl-support
            	        if ( (LA19_0==RULE_WS) ) {
            	            alt19=1;
=======
            	        if ( (LA22_0==RULE_WS) ) {
            	            alt22=1;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	        }


<<<<<<< feature/ecl-support
            	        switch (alt19) {
=======
            	        switch (alt22) {
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	    	case 1 :
<<<<<<< feature/ecl-support
            	    	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1349:9: this_WS_1= RULE_WS
=======
            	    	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1600:9: this_WS_1= RULE_WS
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	    	    {
<<<<<<< feature/ecl-support
            	    	    this_WS_1=(Token)match(input,RULE_WS,FOLLOW_RULE_WS_in_ruleTerm3259); 
=======
            	    	    this_WS_1=(Token)match(input,RULE_WS,FOLLOW_RULE_WS_in_ruleTerm3825); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

            	    	    		current.merge(this_WS_1);
            	    	        
            	    	     
            	    	        newLeafNode(this_WS_1, grammarAccess.getTermAccess().getWSTerminalRuleCall_1_0()); 
            	    	        

            	    	    }
            	    	    break;

            	    	default :
<<<<<<< feature/ecl-support
            	    	    if ( cnt19 >= 1 ) break loop19;
=======
            	    	    if ( cnt22 >= 1 ) break loop22;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	                EarlyExitException eee =
<<<<<<< feature/ecl-support
            	                    new EarlyExitException(19, input);
=======
            	                    new EarlyExitException(22, input);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	                throw eee;
            	        }
<<<<<<< feature/ecl-support
            	        cnt19++;
=======
            	        cnt22++;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	    } while (true);

<<<<<<< feature/ecl-support
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1356:3: (this_TermCharacter_2= ruleTermCharacter )+
            	    int cnt20=0;
            	    loop20:
=======
            	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1607:3: (this_TermCharacter_2= ruleTermCharacter )+
            	    int cnt23=0;
            	    loop23:
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	    do {
<<<<<<< feature/ecl-support
            	        int alt20=2;
            	        int LA20_0 = input.LA(1);
=======
            	        int alt23=2;
            	        int LA23_0 = input.LA(1);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

<<<<<<< feature/ecl-support
            	        if ( ((LA20_0>=RULE_OR && LA20_0<=RULE_COMMA)||(LA20_0>=RULE_COLON && LA20_0<=RULE_CARET)||(LA20_0>=RULE_REVERSED && LA20_0<=RULE_EQUAL)||(LA20_0>=RULE_ROUND_OPEN && LA20_0<=RULE_ZERO)||(LA20_0>=RULE_NOT && LA20_0<=RULE_OTHER_CHARACTER)) ) {
            	            alt20=1;
=======
            	        if ( ((LA23_0>=RULE_OR && LA23_0<=RULE_CARET)||(LA23_0>=RULE_WILDCARD && LA23_0<=RULE_ZERO)||(LA23_0>=RULE_NOT && LA23_0<=RULE_OTHER_CHARACTER)) ) {
            	            alt23=1;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	        }


<<<<<<< feature/ecl-support
            	        switch (alt20) {
=======
            	        switch (alt23) {
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	    	case 1 :
<<<<<<< feature/ecl-support
            	    	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1357:5: this_TermCharacter_2= ruleTermCharacter
=======
            	    	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1608:5: this_TermCharacter_2= ruleTermCharacter
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	    	    {
            	    	     
            	    	            newCompositeNode(grammarAccess.getTermAccess().getTermCharacterParserRuleCall_1_1()); 
            	    	        
<<<<<<< feature/ecl-support
            	    	    pushFollow(FOLLOW_ruleTermCharacter_in_ruleTerm3289);
=======
            	    	    pushFollow(FOLLOW_ruleTermCharacter_in_ruleTerm3855);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	    	    this_TermCharacter_2=ruleTermCharacter();

            	    	    state._fsp--;


            	    	    		current.merge(this_TermCharacter_2);
            	    	        
            	    	     
            	    	            afterParserOrEnumRuleCall();
            	    	        

            	    	    }
            	    	    break;

            	    	default :
<<<<<<< feature/ecl-support
            	    	    if ( cnt20 >= 1 ) break loop20;
=======
            	    	    if ( cnt23 >= 1 ) break loop23;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	                EarlyExitException eee =
<<<<<<< feature/ecl-support
            	                    new EarlyExitException(20, input);
=======
            	                    new EarlyExitException(23, input);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	                throw eee;
            	        }
<<<<<<< feature/ecl-support
            	        cnt20++;
=======
            	        cnt23++;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            	    } while (true);


            	    }
            	    break;

            	default :
<<<<<<< feature/ecl-support
            	    break loop21;
=======
            	    break loop24;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "ruleTerm"


    // $ANTLR start "entryRuleTermCharacter"
<<<<<<< feature/ecl-support
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1378:1: entryRuleTermCharacter returns [String current=null] : iv_ruleTermCharacter= ruleTermCharacter EOF ;
=======
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1629:1: entryRuleTermCharacter returns [String current=null] : iv_ruleTermCharacter= ruleTermCharacter EOF ;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public final String entryRuleTermCharacter() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleTermCharacter = null;


         
        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();
        	
        try {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1382:2: (iv_ruleTermCharacter= ruleTermCharacter EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1383:2: iv_ruleTermCharacter= ruleTermCharacter EOF
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1633:2: (iv_ruleTermCharacter= ruleTermCharacter EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1634:2: iv_ruleTermCharacter= ruleTermCharacter EOF
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
             newCompositeNode(grammarAccess.getTermCharacterRule()); 
<<<<<<< feature/ecl-support
            pushFollow(FOLLOW_ruleTermCharacter_in_entryRuleTermCharacter3349);
=======
            pushFollow(FOLLOW_ruleTermCharacter_in_entryRuleTermCharacter3915);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            iv_ruleTermCharacter=ruleTermCharacter();

            state._fsp--;

             current =iv_ruleTermCharacter.getText(); 
<<<<<<< feature/ecl-support
            match(input,EOF,FOLLOW_EOF_in_entryRuleTermCharacter3360); 
=======
            match(input,EOF,FOLLOW_EOF_in_entryRuleTermCharacter3926); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "entryRuleTermCharacter"


    // $ANTLR start "ruleTermCharacter"
<<<<<<< feature/ecl-support
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1393:1: ruleTermCharacter returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_LT_0= RULE_LT | this_GT_1= RULE_GT | this_DBL_LT_2= RULE_DBL_LT | this_DBL_GT_3= RULE_DBL_GT | this_LT_EM_4= RULE_LT_EM | this_GT_EM_5= RULE_GT_EM | this_AND_6= RULE_AND | this_OR_7= RULE_OR | this_NOT_8= RULE_NOT | this_ZERO_9= RULE_ZERO | this_DIGIT_NONZERO_10= RULE_DIGIT_NONZERO | this_LETTER_11= RULE_LETTER | this_CARET_12= RULE_CARET | this_EQUAL_13= RULE_EQUAL | this_PLUS_14= RULE_PLUS | this_CURLY_OPEN_15= RULE_CURLY_OPEN | this_CURLY_CLOSE_16= RULE_CURLY_CLOSE | this_ROUND_OPEN_17= RULE_ROUND_OPEN | this_ROUND_CLOSE_18= RULE_ROUND_CLOSE | this_SQUARE_OPEN_19= RULE_SQUARE_OPEN | this_SQUARE_CLOSE_20= RULE_SQUARE_CLOSE | this_DOT_21= RULE_DOT | this_COLON_22= RULE_COLON | this_COMMA_23= RULE_COMMA | this_REVERSED_24= RULE_REVERSED | this_OTHER_CHARACTER_25= RULE_OTHER_CHARACTER ) ;
=======
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1644:1: ruleTermCharacter returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_LT_0= RULE_LT | this_GT_1= RULE_GT | this_DBL_LT_2= RULE_DBL_LT | this_DBL_GT_3= RULE_DBL_GT | this_LT_EM_4= RULE_LT_EM | this_GT_EM_5= RULE_GT_EM | this_AND_6= RULE_AND | this_OR_7= RULE_OR | this_NOT_8= RULE_NOT | this_MINUS_9= RULE_MINUS | this_ZERO_10= RULE_ZERO | this_DIGIT_NONZERO_11= RULE_DIGIT_NONZERO | this_LETTER_12= RULE_LETTER | this_CARET_13= RULE_CARET | this_EQUAL_14= RULE_EQUAL | this_NOT_EQUAL_15= RULE_NOT_EQUAL | this_PLUS_16= RULE_PLUS | this_CURLY_OPEN_17= RULE_CURLY_OPEN | this_CURLY_CLOSE_18= RULE_CURLY_CLOSE | this_ROUND_OPEN_19= RULE_ROUND_OPEN | this_ROUND_CLOSE_20= RULE_ROUND_CLOSE | this_SQUARE_OPEN_21= RULE_SQUARE_OPEN | this_SQUARE_CLOSE_22= RULE_SQUARE_CLOSE | this_DOT_23= RULE_DOT | this_COLON_24= RULE_COLON | this_COMMA_25= RULE_COMMA | this_REVERSED_26= RULE_REVERSED | this_TO_27= RULE_TO | this_WILDCARD_28= RULE_WILDCARD | this_OTHER_CHARACTER_29= RULE_OTHER_CHARACTER ) ;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public final AntlrDatatypeRuleToken ruleTermCharacter() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_LT_0=null;
        Token this_GT_1=null;
        Token this_DBL_LT_2=null;
        Token this_DBL_GT_3=null;
        Token this_LT_EM_4=null;
        Token this_GT_EM_5=null;
        Token this_AND_6=null;
        Token this_OR_7=null;
        Token this_NOT_8=null;
        Token this_ZERO_9=null;
        Token this_DIGIT_NONZERO_10=null;
        Token this_LETTER_11=null;
        Token this_CARET_12=null;
        Token this_EQUAL_13=null;
        Token this_PLUS_14=null;
        Token this_CURLY_OPEN_15=null;
        Token this_CURLY_CLOSE_16=null;
        Token this_ROUND_OPEN_17=null;
        Token this_ROUND_CLOSE_18=null;
        Token this_SQUARE_OPEN_19=null;
        Token this_SQUARE_CLOSE_20=null;
        Token this_DOT_21=null;
        Token this_COLON_22=null;
        Token this_COMMA_23=null;
        Token this_REVERSED_24=null;
        Token this_OTHER_CHARACTER_25=null;

         enterRule(); 
        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();
            
        try {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1397:28: ( (this_LT_0= RULE_LT | this_GT_1= RULE_GT | this_DBL_LT_2= RULE_DBL_LT | this_DBL_GT_3= RULE_DBL_GT | this_LT_EM_4= RULE_LT_EM | this_GT_EM_5= RULE_GT_EM | this_AND_6= RULE_AND | this_OR_7= RULE_OR | this_NOT_8= RULE_NOT | this_ZERO_9= RULE_ZERO | this_DIGIT_NONZERO_10= RULE_DIGIT_NONZERO | this_LETTER_11= RULE_LETTER | this_CARET_12= RULE_CARET | this_EQUAL_13= RULE_EQUAL | this_PLUS_14= RULE_PLUS | this_CURLY_OPEN_15= RULE_CURLY_OPEN | this_CURLY_CLOSE_16= RULE_CURLY_CLOSE | this_ROUND_OPEN_17= RULE_ROUND_OPEN | this_ROUND_CLOSE_18= RULE_ROUND_CLOSE | this_SQUARE_OPEN_19= RULE_SQUARE_OPEN | this_SQUARE_CLOSE_20= RULE_SQUARE_CLOSE | this_DOT_21= RULE_DOT | this_COLON_22= RULE_COLON | this_COMMA_23= RULE_COMMA | this_REVERSED_24= RULE_REVERSED | this_OTHER_CHARACTER_25= RULE_OTHER_CHARACTER ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1398:1: (this_LT_0= RULE_LT | this_GT_1= RULE_GT | this_DBL_LT_2= RULE_DBL_LT | this_DBL_GT_3= RULE_DBL_GT | this_LT_EM_4= RULE_LT_EM | this_GT_EM_5= RULE_GT_EM | this_AND_6= RULE_AND | this_OR_7= RULE_OR | this_NOT_8= RULE_NOT | this_ZERO_9= RULE_ZERO | this_DIGIT_NONZERO_10= RULE_DIGIT_NONZERO | this_LETTER_11= RULE_LETTER | this_CARET_12= RULE_CARET | this_EQUAL_13= RULE_EQUAL | this_PLUS_14= RULE_PLUS | this_CURLY_OPEN_15= RULE_CURLY_OPEN | this_CURLY_CLOSE_16= RULE_CURLY_CLOSE | this_ROUND_OPEN_17= RULE_ROUND_OPEN | this_ROUND_CLOSE_18= RULE_ROUND_CLOSE | this_SQUARE_OPEN_19= RULE_SQUARE_OPEN | this_SQUARE_CLOSE_20= RULE_SQUARE_CLOSE | this_DOT_21= RULE_DOT | this_COLON_22= RULE_COLON | this_COMMA_23= RULE_COMMA | this_REVERSED_24= RULE_REVERSED | this_OTHER_CHARACTER_25= RULE_OTHER_CHARACTER )
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1648:28: ( (this_LT_0= RULE_LT | this_GT_1= RULE_GT | this_DBL_LT_2= RULE_DBL_LT | this_DBL_GT_3= RULE_DBL_GT | this_LT_EM_4= RULE_LT_EM | this_GT_EM_5= RULE_GT_EM | this_AND_6= RULE_AND | this_OR_7= RULE_OR | this_NOT_8= RULE_NOT | this_MINUS_9= RULE_MINUS | this_ZERO_10= RULE_ZERO | this_DIGIT_NONZERO_11= RULE_DIGIT_NONZERO | this_LETTER_12= RULE_LETTER | this_CARET_13= RULE_CARET | this_EQUAL_14= RULE_EQUAL | this_NOT_EQUAL_15= RULE_NOT_EQUAL | this_PLUS_16= RULE_PLUS | this_CURLY_OPEN_17= RULE_CURLY_OPEN | this_CURLY_CLOSE_18= RULE_CURLY_CLOSE | this_ROUND_OPEN_19= RULE_ROUND_OPEN | this_ROUND_CLOSE_20= RULE_ROUND_CLOSE | this_SQUARE_OPEN_21= RULE_SQUARE_OPEN | this_SQUARE_CLOSE_22= RULE_SQUARE_CLOSE | this_DOT_23= RULE_DOT | this_COLON_24= RULE_COLON | this_COMMA_25= RULE_COMMA | this_REVERSED_26= RULE_REVERSED | this_TO_27= RULE_TO | this_WILDCARD_28= RULE_WILDCARD | this_OTHER_CHARACTER_29= RULE_OTHER_CHARACTER ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1649:1: (this_LT_0= RULE_LT | this_GT_1= RULE_GT | this_DBL_LT_2= RULE_DBL_LT | this_DBL_GT_3= RULE_DBL_GT | this_LT_EM_4= RULE_LT_EM | this_GT_EM_5= RULE_GT_EM | this_AND_6= RULE_AND | this_OR_7= RULE_OR | this_NOT_8= RULE_NOT | this_MINUS_9= RULE_MINUS | this_ZERO_10= RULE_ZERO | this_DIGIT_NONZERO_11= RULE_DIGIT_NONZERO | this_LETTER_12= RULE_LETTER | this_CARET_13= RULE_CARET | this_EQUAL_14= RULE_EQUAL | this_NOT_EQUAL_15= RULE_NOT_EQUAL | this_PLUS_16= RULE_PLUS | this_CURLY_OPEN_17= RULE_CURLY_OPEN | this_CURLY_CLOSE_18= RULE_CURLY_CLOSE | this_ROUND_OPEN_19= RULE_ROUND_OPEN | this_ROUND_CLOSE_20= RULE_ROUND_CLOSE | this_SQUARE_OPEN_21= RULE_SQUARE_OPEN | this_SQUARE_CLOSE_22= RULE_SQUARE_CLOSE | this_DOT_23= RULE_DOT | this_COLON_24= RULE_COLON | this_COMMA_25= RULE_COMMA | this_REVERSED_26= RULE_REVERSED | this_TO_27= RULE_TO | this_WILDCARD_28= RULE_WILDCARD | this_OTHER_CHARACTER_29= RULE_OTHER_CHARACTER )
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            {
<<<<<<< feature/ecl-support
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1398:1: (this_LT_0= RULE_LT | this_GT_1= RULE_GT | this_DBL_LT_2= RULE_DBL_LT | this_DBL_GT_3= RULE_DBL_GT | this_LT_EM_4= RULE_LT_EM | this_GT_EM_5= RULE_GT_EM | this_AND_6= RULE_AND | this_OR_7= RULE_OR | this_NOT_8= RULE_NOT | this_ZERO_9= RULE_ZERO | this_DIGIT_NONZERO_10= RULE_DIGIT_NONZERO | this_LETTER_11= RULE_LETTER | this_CARET_12= RULE_CARET | this_EQUAL_13= RULE_EQUAL | this_PLUS_14= RULE_PLUS | this_CURLY_OPEN_15= RULE_CURLY_OPEN | this_CURLY_CLOSE_16= RULE_CURLY_CLOSE | this_ROUND_OPEN_17= RULE_ROUND_OPEN | this_ROUND_CLOSE_18= RULE_ROUND_CLOSE | this_SQUARE_OPEN_19= RULE_SQUARE_OPEN | this_SQUARE_CLOSE_20= RULE_SQUARE_CLOSE | this_DOT_21= RULE_DOT | this_COLON_22= RULE_COLON | this_COMMA_23= RULE_COMMA | this_REVERSED_24= RULE_REVERSED | this_OTHER_CHARACTER_25= RULE_OTHER_CHARACTER )
            int alt22=26;
=======
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1649:1: (this_LT_0= RULE_LT | this_GT_1= RULE_GT | this_DBL_LT_2= RULE_DBL_LT | this_DBL_GT_3= RULE_DBL_GT | this_LT_EM_4= RULE_LT_EM | this_GT_EM_5= RULE_GT_EM | this_AND_6= RULE_AND | this_OR_7= RULE_OR | this_NOT_8= RULE_NOT | this_MINUS_9= RULE_MINUS | this_ZERO_10= RULE_ZERO | this_DIGIT_NONZERO_11= RULE_DIGIT_NONZERO | this_LETTER_12= RULE_LETTER | this_CARET_13= RULE_CARET | this_EQUAL_14= RULE_EQUAL | this_NOT_EQUAL_15= RULE_NOT_EQUAL | this_PLUS_16= RULE_PLUS | this_CURLY_OPEN_17= RULE_CURLY_OPEN | this_CURLY_CLOSE_18= RULE_CURLY_CLOSE | this_ROUND_OPEN_19= RULE_ROUND_OPEN | this_ROUND_CLOSE_20= RULE_ROUND_CLOSE | this_SQUARE_OPEN_21= RULE_SQUARE_OPEN | this_SQUARE_CLOSE_22= RULE_SQUARE_CLOSE | this_DOT_23= RULE_DOT | this_COLON_24= RULE_COLON | this_COMMA_25= RULE_COMMA | this_REVERSED_26= RULE_REVERSED | this_TO_27= RULE_TO | this_WILDCARD_28= RULE_WILDCARD | this_OTHER_CHARACTER_29= RULE_OTHER_CHARACTER )
            int alt25=30;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
            switch ( input.LA(1) ) {
            case RULE_LT:
                {
<<<<<<< feature/ecl-support
                alt22=1;
=======
                alt25=1;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_GT:
                {
<<<<<<< feature/ecl-support
                alt22=2;
=======
                alt25=2;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_DBL_LT:
                {
<<<<<<< feature/ecl-support
                alt22=3;
=======
                alt25=3;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_DBL_GT:
                {
<<<<<<< feature/ecl-support
                alt22=4;
=======
                alt25=4;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_LT_EM:
                {
<<<<<<< feature/ecl-support
                alt22=5;
=======
                alt25=5;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_GT_EM:
                {
<<<<<<< feature/ecl-support
                alt22=6;
=======
                alt25=6;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_AND:
                {
<<<<<<< feature/ecl-support
                alt22=7;
=======
                alt25=7;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_OR:
                {
<<<<<<< feature/ecl-support
                alt22=8;
=======
                alt25=8;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_NOT:
                {
<<<<<<< feature/ecl-support
                alt22=9;
=======
                alt25=9;
                }
                break;
            case RULE_MINUS:
                {
                alt25=10;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_ZERO:
                {
<<<<<<< feature/ecl-support
                alt22=10;
=======
                alt25=11;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_DIGIT_NONZERO:
                {
<<<<<<< feature/ecl-support
                alt22=11;
=======
                alt25=12;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_LETTER:
                {
<<<<<<< feature/ecl-support
                alt22=12;
=======
                alt25=13;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_CARET:
                {
<<<<<<< feature/ecl-support
                alt22=13;
=======
                alt25=14;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_EQUAL:
                {
<<<<<<< feature/ecl-support
                alt22=14;
=======
                alt25=15;
                }
                break;
            case RULE_NOT_EQUAL:
                {
                alt25=16;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_PLUS:
                {
<<<<<<< feature/ecl-support
                alt22=15;
=======
                alt25=17;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_CURLY_OPEN:
                {
<<<<<<< feature/ecl-support
                alt22=16;
=======
                alt25=18;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_CURLY_CLOSE:
                {
<<<<<<< feature/ecl-support
                alt22=17;
=======
                alt25=19;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_ROUND_OPEN:
                {
<<<<<<< feature/ecl-support
                alt22=18;
=======
                alt25=20;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_ROUND_CLOSE:
                {
<<<<<<< feature/ecl-support
                alt22=19;
=======
                alt25=21;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_SQUARE_OPEN:
                {
<<<<<<< feature/ecl-support
                alt22=20;
=======
                alt25=22;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_SQUARE_CLOSE:
                {
<<<<<<< feature/ecl-support
                alt22=21;
=======
                alt25=23;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_DOT:
                {
<<<<<<< feature/ecl-support
                alt22=22;
=======
                alt25=24;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_COLON:
                {
<<<<<<< feature/ecl-support
                alt22=23;
=======
                alt25=25;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_COMMA:
                {
<<<<<<< feature/ecl-support
                alt22=24;
=======
                alt25=26;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_REVERSED:
                {
<<<<<<< feature/ecl-support
                alt22=25;
=======
                alt25=27;
                }
                break;
            case RULE_TO:
                {
                alt25=28;
                }
                break;
            case RULE_WILDCARD:
                {
                alt25=29;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            case RULE_OTHER_CHARACTER:
                {
<<<<<<< feature/ecl-support
                alt22=26;
=======
                alt25=30;
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                }
                break;
            default:
                NoViableAltException nvae =
<<<<<<< feature/ecl-support
                    new NoViableAltException("", 22, 0, input);
=======
                    new NoViableAltException("", 25, 0, input);
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                throw nvae;
            }

<<<<<<< feature/ecl-support
            switch (alt22) {
=======
            switch (alt25) {
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                case 1 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1398:6: this_LT_0= RULE_LT
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1649:6: this_LT_0= RULE_LT
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_LT_0=(Token)match(input,RULE_LT,FOLLOW_RULE_LT_in_ruleTermCharacter3404); 
=======
                    this_LT_0=(Token)match(input,RULE_LT,FOLLOW_RULE_LT_in_ruleTermCharacter3970); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_LT_0);
                        
                     
                        newLeafNode(this_LT_0, grammarAccess.getTermCharacterAccess().getLTTerminalRuleCall_0()); 
                        

                    }
                    break;
                case 2 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1406:10: this_GT_1= RULE_GT
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1657:10: this_GT_1= RULE_GT
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_GT_1=(Token)match(input,RULE_GT,FOLLOW_RULE_GT_in_ruleTermCharacter3430); 
=======
                    this_GT_1=(Token)match(input,RULE_GT,FOLLOW_RULE_GT_in_ruleTermCharacter3996); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_GT_1);
                        
                     
                        newLeafNode(this_GT_1, grammarAccess.getTermCharacterAccess().getGTTerminalRuleCall_1()); 
                        

                    }
                    break;
                case 3 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1414:10: this_DBL_LT_2= RULE_DBL_LT
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1665:10: this_DBL_LT_2= RULE_DBL_LT
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_DBL_LT_2=(Token)match(input,RULE_DBL_LT,FOLLOW_RULE_DBL_LT_in_ruleTermCharacter3456); 
=======
                    this_DBL_LT_2=(Token)match(input,RULE_DBL_LT,FOLLOW_RULE_DBL_LT_in_ruleTermCharacter4022); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_DBL_LT_2);
                        
                     
                        newLeafNode(this_DBL_LT_2, grammarAccess.getTermCharacterAccess().getDBL_LTTerminalRuleCall_2()); 
                        

                    }
                    break;
                case 4 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1422:10: this_DBL_GT_3= RULE_DBL_GT
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1673:10: this_DBL_GT_3= RULE_DBL_GT
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_DBL_GT_3=(Token)match(input,RULE_DBL_GT,FOLLOW_RULE_DBL_GT_in_ruleTermCharacter3482); 
=======
                    this_DBL_GT_3=(Token)match(input,RULE_DBL_GT,FOLLOW_RULE_DBL_GT_in_ruleTermCharacter4048); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_DBL_GT_3);
                        
                     
                        newLeafNode(this_DBL_GT_3, grammarAccess.getTermCharacterAccess().getDBL_GTTerminalRuleCall_3()); 
                        

                    }
                    break;
                case 5 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1430:10: this_LT_EM_4= RULE_LT_EM
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1681:10: this_LT_EM_4= RULE_LT_EM
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_LT_EM_4=(Token)match(input,RULE_LT_EM,FOLLOW_RULE_LT_EM_in_ruleTermCharacter3508); 
=======
                    this_LT_EM_4=(Token)match(input,RULE_LT_EM,FOLLOW_RULE_LT_EM_in_ruleTermCharacter4074); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_LT_EM_4);
                        
                     
                        newLeafNode(this_LT_EM_4, grammarAccess.getTermCharacterAccess().getLT_EMTerminalRuleCall_4()); 
                        

                    }
                    break;
                case 6 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1438:10: this_GT_EM_5= RULE_GT_EM
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1689:10: this_GT_EM_5= RULE_GT_EM
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_GT_EM_5=(Token)match(input,RULE_GT_EM,FOLLOW_RULE_GT_EM_in_ruleTermCharacter3534); 
=======
                    this_GT_EM_5=(Token)match(input,RULE_GT_EM,FOLLOW_RULE_GT_EM_in_ruleTermCharacter4100); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_GT_EM_5);
                        
                     
                        newLeafNode(this_GT_EM_5, grammarAccess.getTermCharacterAccess().getGT_EMTerminalRuleCall_5()); 
                        

                    }
                    break;
                case 7 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1446:10: this_AND_6= RULE_AND
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1697:10: this_AND_6= RULE_AND
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_AND_6=(Token)match(input,RULE_AND,FOLLOW_RULE_AND_in_ruleTermCharacter3560); 
=======
                    this_AND_6=(Token)match(input,RULE_AND,FOLLOW_RULE_AND_in_ruleTermCharacter4126); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_AND_6);
                        
                     
                        newLeafNode(this_AND_6, grammarAccess.getTermCharacterAccess().getANDTerminalRuleCall_6()); 
                        

                    }
                    break;
                case 8 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1454:10: this_OR_7= RULE_OR
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1705:10: this_OR_7= RULE_OR
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_OR_7=(Token)match(input,RULE_OR,FOLLOW_RULE_OR_in_ruleTermCharacter3586); 
=======
                    this_OR_7=(Token)match(input,RULE_OR,FOLLOW_RULE_OR_in_ruleTermCharacter4152); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_OR_7);
                        
                     
                        newLeafNode(this_OR_7, grammarAccess.getTermCharacterAccess().getORTerminalRuleCall_7()); 
                        

                    }
                    break;
                case 9 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1462:10: this_NOT_8= RULE_NOT
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1713:10: this_NOT_8= RULE_NOT
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_NOT_8=(Token)match(input,RULE_NOT,FOLLOW_RULE_NOT_in_ruleTermCharacter3612); 
=======
                    this_NOT_8=(Token)match(input,RULE_NOT,FOLLOW_RULE_NOT_in_ruleTermCharacter4178); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_NOT_8);
                        
                     
                        newLeafNode(this_NOT_8, grammarAccess.getTermCharacterAccess().getNOTTerminalRuleCall_8()); 
                        

                    }
                    break;
                case 10 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1470:10: this_ZERO_9= RULE_ZERO
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1721:10: this_MINUS_9= RULE_MINUS
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_ZERO_9=(Token)match(input,RULE_ZERO,FOLLOW_RULE_ZERO_in_ruleTermCharacter3638); 
=======
                    this_MINUS_9=(Token)match(input,RULE_MINUS,FOLLOW_RULE_MINUS_in_ruleTermCharacter4204); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_ZERO_9);
                        
                     
                        newLeafNode(this_ZERO_9, grammarAccess.getTermCharacterAccess().getZEROTerminalRuleCall_9()); 
                        

                    }
                    break;
                case 11 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1478:10: this_DIGIT_NONZERO_10= RULE_DIGIT_NONZERO
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1729:10: this_ZERO_10= RULE_ZERO
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_DIGIT_NONZERO_10=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleTermCharacter3664); 
=======
                    this_ZERO_10=(Token)match(input,RULE_ZERO,FOLLOW_RULE_ZERO_in_ruleTermCharacter4230); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_DIGIT_NONZERO_10);
                        
                     
                        newLeafNode(this_DIGIT_NONZERO_10, grammarAccess.getTermCharacterAccess().getDIGIT_NONZEROTerminalRuleCall_10()); 
                        

                    }
                    break;
                case 12 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1486:10: this_LETTER_11= RULE_LETTER
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1737:10: this_DIGIT_NONZERO_11= RULE_DIGIT_NONZERO
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_LETTER_11=(Token)match(input,RULE_LETTER,FOLLOW_RULE_LETTER_in_ruleTermCharacter3690); 
=======
                    this_DIGIT_NONZERO_11=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleTermCharacter4256); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_LETTER_11);
                        
                     
                        newLeafNode(this_LETTER_11, grammarAccess.getTermCharacterAccess().getLETTERTerminalRuleCall_11()); 
                        

                    }
                    break;
                case 13 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1494:10: this_CARET_12= RULE_CARET
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1745:10: this_LETTER_12= RULE_LETTER
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_CARET_12=(Token)match(input,RULE_CARET,FOLLOW_RULE_CARET_in_ruleTermCharacter3716); 
=======
                    this_LETTER_12=(Token)match(input,RULE_LETTER,FOLLOW_RULE_LETTER_in_ruleTermCharacter4282); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_CARET_12);
                        
                     
                        newLeafNode(this_CARET_12, grammarAccess.getTermCharacterAccess().getCARETTerminalRuleCall_12()); 
                        

                    }
                    break;
                case 14 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1502:10: this_EQUAL_13= RULE_EQUAL
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1753:10: this_CARET_13= RULE_CARET
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_EQUAL_13=(Token)match(input,RULE_EQUAL,FOLLOW_RULE_EQUAL_in_ruleTermCharacter3742); 
=======
                    this_CARET_13=(Token)match(input,RULE_CARET,FOLLOW_RULE_CARET_in_ruleTermCharacter4308); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_EQUAL_13);
                        
                     
                        newLeafNode(this_EQUAL_13, grammarAccess.getTermCharacterAccess().getEQUALTerminalRuleCall_13()); 
                        

                    }
                    break;
                case 15 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1510:10: this_PLUS_14= RULE_PLUS
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1761:10: this_EQUAL_14= RULE_EQUAL
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_PLUS_14=(Token)match(input,RULE_PLUS,FOLLOW_RULE_PLUS_in_ruleTermCharacter3768); 
=======
                    this_EQUAL_14=(Token)match(input,RULE_EQUAL,FOLLOW_RULE_EQUAL_in_ruleTermCharacter4334); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_PLUS_14);
                        
                     
                        newLeafNode(this_PLUS_14, grammarAccess.getTermCharacterAccess().getPLUSTerminalRuleCall_14()); 
                        

                    }
                    break;
                case 16 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1518:10: this_CURLY_OPEN_15= RULE_CURLY_OPEN
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1769:10: this_NOT_EQUAL_15= RULE_NOT_EQUAL
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_CURLY_OPEN_15=(Token)match(input,RULE_CURLY_OPEN,FOLLOW_RULE_CURLY_OPEN_in_ruleTermCharacter3794); 
=======
                    this_NOT_EQUAL_15=(Token)match(input,RULE_NOT_EQUAL,FOLLOW_RULE_NOT_EQUAL_in_ruleTermCharacter4360); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_CURLY_OPEN_15);
                        
                     
                        newLeafNode(this_CURLY_OPEN_15, grammarAccess.getTermCharacterAccess().getCURLY_OPENTerminalRuleCall_15()); 
                        

                    }
                    break;
                case 17 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1526:10: this_CURLY_CLOSE_16= RULE_CURLY_CLOSE
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1777:10: this_PLUS_16= RULE_PLUS
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_CURLY_CLOSE_16=(Token)match(input,RULE_CURLY_CLOSE,FOLLOW_RULE_CURLY_CLOSE_in_ruleTermCharacter3820); 
=======
                    this_PLUS_16=(Token)match(input,RULE_PLUS,FOLLOW_RULE_PLUS_in_ruleTermCharacter4386); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_CURLY_CLOSE_16);
                        
                     
                        newLeafNode(this_CURLY_CLOSE_16, grammarAccess.getTermCharacterAccess().getCURLY_CLOSETerminalRuleCall_16()); 
                        

                    }
                    break;
                case 18 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1534:10: this_ROUND_OPEN_17= RULE_ROUND_OPEN
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1785:10: this_CURLY_OPEN_17= RULE_CURLY_OPEN
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_ROUND_OPEN_17=(Token)match(input,RULE_ROUND_OPEN,FOLLOW_RULE_ROUND_OPEN_in_ruleTermCharacter3846); 
=======
                    this_CURLY_OPEN_17=(Token)match(input,RULE_CURLY_OPEN,FOLLOW_RULE_CURLY_OPEN_in_ruleTermCharacter4412); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_ROUND_OPEN_17);
                        
                     
                        newLeafNode(this_ROUND_OPEN_17, grammarAccess.getTermCharacterAccess().getROUND_OPENTerminalRuleCall_17()); 
                        

                    }
                    break;
                case 19 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1542:10: this_ROUND_CLOSE_18= RULE_ROUND_CLOSE
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1793:10: this_CURLY_CLOSE_18= RULE_CURLY_CLOSE
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_ROUND_CLOSE_18=(Token)match(input,RULE_ROUND_CLOSE,FOLLOW_RULE_ROUND_CLOSE_in_ruleTermCharacter3872); 
=======
                    this_CURLY_CLOSE_18=(Token)match(input,RULE_CURLY_CLOSE,FOLLOW_RULE_CURLY_CLOSE_in_ruleTermCharacter4438); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_ROUND_CLOSE_18);
                        
                     
                        newLeafNode(this_ROUND_CLOSE_18, grammarAccess.getTermCharacterAccess().getROUND_CLOSETerminalRuleCall_18()); 
                        

                    }
                    break;
                case 20 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1550:10: this_SQUARE_OPEN_19= RULE_SQUARE_OPEN
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1801:10: this_ROUND_OPEN_19= RULE_ROUND_OPEN
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_SQUARE_OPEN_19=(Token)match(input,RULE_SQUARE_OPEN,FOLLOW_RULE_SQUARE_OPEN_in_ruleTermCharacter3898); 
=======
                    this_ROUND_OPEN_19=(Token)match(input,RULE_ROUND_OPEN,FOLLOW_RULE_ROUND_OPEN_in_ruleTermCharacter4464); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_SQUARE_OPEN_19);
                        
                     
                        newLeafNode(this_SQUARE_OPEN_19, grammarAccess.getTermCharacterAccess().getSQUARE_OPENTerminalRuleCall_19()); 
                        

                    }
                    break;
                case 21 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1558:10: this_SQUARE_CLOSE_20= RULE_SQUARE_CLOSE
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1809:10: this_ROUND_CLOSE_20= RULE_ROUND_CLOSE
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_SQUARE_CLOSE_20=(Token)match(input,RULE_SQUARE_CLOSE,FOLLOW_RULE_SQUARE_CLOSE_in_ruleTermCharacter3924); 
=======
                    this_ROUND_CLOSE_20=(Token)match(input,RULE_ROUND_CLOSE,FOLLOW_RULE_ROUND_CLOSE_in_ruleTermCharacter4490); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_SQUARE_CLOSE_20);
                        
                     
                        newLeafNode(this_SQUARE_CLOSE_20, grammarAccess.getTermCharacterAccess().getSQUARE_CLOSETerminalRuleCall_20()); 
                        

                    }
                    break;
                case 22 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1566:10: this_DOT_21= RULE_DOT
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1817:10: this_SQUARE_OPEN_21= RULE_SQUARE_OPEN
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_DOT_21=(Token)match(input,RULE_DOT,FOLLOW_RULE_DOT_in_ruleTermCharacter3950); 
=======
                    this_SQUARE_OPEN_21=(Token)match(input,RULE_SQUARE_OPEN,FOLLOW_RULE_SQUARE_OPEN_in_ruleTermCharacter4516); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_DOT_21);
                        
                     
                        newLeafNode(this_DOT_21, grammarAccess.getTermCharacterAccess().getDOTTerminalRuleCall_21()); 
                        

                    }
                    break;
                case 23 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1574:10: this_COLON_22= RULE_COLON
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1825:10: this_SQUARE_CLOSE_22= RULE_SQUARE_CLOSE
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_COLON_22=(Token)match(input,RULE_COLON,FOLLOW_RULE_COLON_in_ruleTermCharacter3976); 
=======
                    this_SQUARE_CLOSE_22=(Token)match(input,RULE_SQUARE_CLOSE,FOLLOW_RULE_SQUARE_CLOSE_in_ruleTermCharacter4542); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_COLON_22);
                        
                     
                        newLeafNode(this_COLON_22, grammarAccess.getTermCharacterAccess().getCOLONTerminalRuleCall_22()); 
                        

                    }
                    break;
                case 24 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1582:10: this_COMMA_23= RULE_COMMA
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1833:10: this_DOT_23= RULE_DOT
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_COMMA_23=(Token)match(input,RULE_COMMA,FOLLOW_RULE_COMMA_in_ruleTermCharacter4002); 
=======
                    this_DOT_23=(Token)match(input,RULE_DOT,FOLLOW_RULE_DOT_in_ruleTermCharacter4568); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_COMMA_23);
                        
                     
                        newLeafNode(this_COMMA_23, grammarAccess.getTermCharacterAccess().getCOMMATerminalRuleCall_23()); 
                        

                    }
                    break;
                case 25 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1590:10: this_REVERSED_24= RULE_REVERSED
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1841:10: this_COLON_24= RULE_COLON
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_REVERSED_24=(Token)match(input,RULE_REVERSED,FOLLOW_RULE_REVERSED_in_ruleTermCharacter4028); 
=======
                    this_COLON_24=(Token)match(input,RULE_COLON,FOLLOW_RULE_COLON_in_ruleTermCharacter4594); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_REVERSED_24);
                        
                     
                        newLeafNode(this_REVERSED_24, grammarAccess.getTermCharacterAccess().getREVERSEDTerminalRuleCall_24()); 
                        

                    }
                    break;
                case 26 :
<<<<<<< feature/ecl-support
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1598:10: this_OTHER_CHARACTER_25= RULE_OTHER_CHARACTER
=======
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1849:10: this_COMMA_25= RULE_COMMA
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                    {
<<<<<<< feature/ecl-support
                    this_OTHER_CHARACTER_25=(Token)match(input,RULE_OTHER_CHARACTER,FOLLOW_RULE_OTHER_CHARACTER_in_ruleTermCharacter4054); 
=======
                    this_COMMA_25=(Token)match(input,RULE_COMMA,FOLLOW_RULE_COMMA_in_ruleTermCharacter4620); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

                    		current.merge(this_OTHER_CHARACTER_25);
                        
                     
<<<<<<< feature/ecl-support
                        newLeafNode(this_OTHER_CHARACTER_25, grammarAccess.getTermCharacterAccess().getOTHER_CHARACTERTerminalRuleCall_25()); 
=======
                        newLeafNode(this_COMMA_25, grammarAccess.getTermCharacterAccess().getCOMMATerminalRuleCall_25()); 
                        

                    }
                    break;
                case 27 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1857:10: this_REVERSED_26= RULE_REVERSED
                    {
                    this_REVERSED_26=(Token)match(input,RULE_REVERSED,FOLLOW_RULE_REVERSED_in_ruleTermCharacter4646); 

                    		current.merge(this_REVERSED_26);
                        
                     
                        newLeafNode(this_REVERSED_26, grammarAccess.getTermCharacterAccess().getREVERSEDTerminalRuleCall_26()); 
                        

                    }
                    break;
                case 28 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1865:10: this_TO_27= RULE_TO
                    {
                    this_TO_27=(Token)match(input,RULE_TO,FOLLOW_RULE_TO_in_ruleTermCharacter4672); 

                    		current.merge(this_TO_27);
                        
                     
                        newLeafNode(this_TO_27, grammarAccess.getTermCharacterAccess().getTOTerminalRuleCall_27()); 
                        

                    }
                    break;
                case 29 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1873:10: this_WILDCARD_28= RULE_WILDCARD
                    {
                    this_WILDCARD_28=(Token)match(input,RULE_WILDCARD,FOLLOW_RULE_WILDCARD_in_ruleTermCharacter4698); 

                    		current.merge(this_WILDCARD_28);
                        
                     
                        newLeafNode(this_WILDCARD_28, grammarAccess.getTermCharacterAccess().getWILDCARDTerminalRuleCall_28()); 
                        

                    }
                    break;
                case 30 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1881:10: this_OTHER_CHARACTER_29= RULE_OTHER_CHARACTER
                    {
                    this_OTHER_CHARACTER_29=(Token)match(input,RULE_OTHER_CHARACTER,FOLLOW_RULE_OTHER_CHARACTER_in_ruleTermCharacter4724); 

                    		current.merge(this_OTHER_CHARACTER_29);
                        
                     
                        newLeafNode(this_OTHER_CHARACTER_29, grammarAccess.getTermCharacterAccess().getOTHER_CHARACTERTerminalRuleCall_29()); 
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "ruleTermCharacter"

<<<<<<< feature/ecl-support
=======

    // $ANTLR start "entryRuleNonNegativeInteger"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1899:1: entryRuleNonNegativeInteger returns [String current=null] : iv_ruleNonNegativeInteger= ruleNonNegativeInteger EOF ;
    public final String entryRuleNonNegativeInteger() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleNonNegativeInteger = null;


         
        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();
        	
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1903:2: (iv_ruleNonNegativeInteger= ruleNonNegativeInteger EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1904:2: iv_ruleNonNegativeInteger= ruleNonNegativeInteger EOF
            {
             newCompositeNode(grammarAccess.getNonNegativeIntegerRule()); 
            pushFollow(FOLLOW_ruleNonNegativeInteger_in_entryRuleNonNegativeInteger4780);
            iv_ruleNonNegativeInteger=ruleNonNegativeInteger();

            state._fsp--;

             current =iv_ruleNonNegativeInteger.getText(); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleNonNegativeInteger4791); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "entryRuleNonNegativeInteger"


    // $ANTLR start "ruleNonNegativeInteger"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1914:1: ruleNonNegativeInteger returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_ZERO_0= RULE_ZERO | (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_2= RULE_DIGIT_NONZERO | this_ZERO_3= RULE_ZERO )* ) ) ;
    public final AntlrDatatypeRuleToken ruleNonNegativeInteger() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_ZERO_0=null;
        Token this_DIGIT_NONZERO_1=null;
        Token this_DIGIT_NONZERO_2=null;
        Token this_ZERO_3=null;

         enterRule(); 
        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1918:28: ( (this_ZERO_0= RULE_ZERO | (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_2= RULE_DIGIT_NONZERO | this_ZERO_3= RULE_ZERO )* ) ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1919:1: (this_ZERO_0= RULE_ZERO | (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_2= RULE_DIGIT_NONZERO | this_ZERO_3= RULE_ZERO )* ) )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1919:1: (this_ZERO_0= RULE_ZERO | (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_2= RULE_DIGIT_NONZERO | this_ZERO_3= RULE_ZERO )* ) )
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==RULE_ZERO) ) {
                alt27=1;
            }
            else if ( (LA27_0==RULE_DIGIT_NONZERO) ) {
                alt27=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;
            }
            switch (alt27) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1919:6: this_ZERO_0= RULE_ZERO
                    {
                    this_ZERO_0=(Token)match(input,RULE_ZERO,FOLLOW_RULE_ZERO_in_ruleNonNegativeInteger4835); 

                    		current.merge(this_ZERO_0);
                        
                     
                        newLeafNode(this_ZERO_0, grammarAccess.getNonNegativeIntegerAccess().getZEROTerminalRuleCall_0()); 
                        

                    }
                    break;
                case 2 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1927:6: (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_2= RULE_DIGIT_NONZERO | this_ZERO_3= RULE_ZERO )* )
                    {
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1927:6: (this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_2= RULE_DIGIT_NONZERO | this_ZERO_3= RULE_ZERO )* )
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1927:11: this_DIGIT_NONZERO_1= RULE_DIGIT_NONZERO (this_DIGIT_NONZERO_2= RULE_DIGIT_NONZERO | this_ZERO_3= RULE_ZERO )*
                    {
                    this_DIGIT_NONZERO_1=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleNonNegativeInteger4862); 

                    		current.merge(this_DIGIT_NONZERO_1);
                        
                     
                        newLeafNode(this_DIGIT_NONZERO_1, grammarAccess.getNonNegativeIntegerAccess().getDIGIT_NONZEROTerminalRuleCall_1_0()); 
                        
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1934:1: (this_DIGIT_NONZERO_2= RULE_DIGIT_NONZERO | this_ZERO_3= RULE_ZERO )*
                    loop26:
                    do {
                        int alt26=3;
                        int LA26_0 = input.LA(1);

                        if ( (LA26_0==RULE_DIGIT_NONZERO) ) {
                            alt26=1;
                        }
                        else if ( (LA26_0==RULE_ZERO) ) {
                            alt26=2;
                        }


                        switch (alt26) {
                    	case 1 :
                    	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1934:6: this_DIGIT_NONZERO_2= RULE_DIGIT_NONZERO
                    	    {
                    	    this_DIGIT_NONZERO_2=(Token)match(input,RULE_DIGIT_NONZERO,FOLLOW_RULE_DIGIT_NONZERO_in_ruleNonNegativeInteger4883); 

                    	    		current.merge(this_DIGIT_NONZERO_2);
                    	        
                    	     
                    	        newLeafNode(this_DIGIT_NONZERO_2, grammarAccess.getNonNegativeIntegerAccess().getDIGIT_NONZEROTerminalRuleCall_1_1_0()); 
                    	        

                    	    }
                    	    break;
                    	case 2 :
                    	    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1942:10: this_ZERO_3= RULE_ZERO
                    	    {
                    	    this_ZERO_3=(Token)match(input,RULE_ZERO,FOLLOW_RULE_ZERO_in_ruleNonNegativeInteger4909); 

                    	    		current.merge(this_ZERO_3);
                    	        
                    	     
                    	        newLeafNode(this_ZERO_3, grammarAccess.getNonNegativeIntegerAccess().getZEROTerminalRuleCall_1_1_1()); 
                    	        

                    	    }
                    	    break;

                    	default :
                    	    break loop26;
                        }
                    } while (true);


                    }


                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "ruleNonNegativeInteger"


    // $ANTLR start "entryRuleMaxValue"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1960:1: entryRuleMaxValue returns [String current=null] : iv_ruleMaxValue= ruleMaxValue EOF ;
    public final String entryRuleMaxValue() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleMaxValue = null;


         
        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();
        	
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1964:2: (iv_ruleMaxValue= ruleMaxValue EOF )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1965:2: iv_ruleMaxValue= ruleMaxValue EOF
            {
             newCompositeNode(grammarAccess.getMaxValueRule()); 
            pushFollow(FOLLOW_ruleMaxValue_in_entryRuleMaxValue4968);
            iv_ruleMaxValue=ruleMaxValue();

            state._fsp--;

             current =iv_ruleMaxValue.getText(); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleMaxValue4979); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "entryRuleMaxValue"


    // $ANTLR start "ruleMaxValue"
    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1975:1: ruleMaxValue returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_NonNegativeInteger_0= ruleNonNegativeInteger | this_WILDCARD_1= RULE_WILDCARD ) ;
    public final AntlrDatatypeRuleToken ruleMaxValue() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_WILDCARD_1=null;
        AntlrDatatypeRuleToken this_NonNegativeInteger_0 = null;


         enterRule(); 
        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();
            
        try {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1979:28: ( (this_NonNegativeInteger_0= ruleNonNegativeInteger | this_WILDCARD_1= RULE_WILDCARD ) )
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1980:1: (this_NonNegativeInteger_0= ruleNonNegativeInteger | this_WILDCARD_1= RULE_WILDCARD )
            {
            // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1980:1: (this_NonNegativeInteger_0= ruleNonNegativeInteger | this_WILDCARD_1= RULE_WILDCARD )
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( ((LA28_0>=RULE_DIGIT_NONZERO && LA28_0<=RULE_ZERO)) ) {
                alt28=1;
            }
            else if ( (LA28_0==RULE_WILDCARD) ) {
                alt28=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;
            }
            switch (alt28) {
                case 1 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1981:5: this_NonNegativeInteger_0= ruleNonNegativeInteger
                    {
                     
                            newCompositeNode(grammarAccess.getMaxValueAccess().getNonNegativeIntegerParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleNonNegativeInteger_in_ruleMaxValue5030);
                    this_NonNegativeInteger_0=ruleNonNegativeInteger();

                    state._fsp--;


                    		current.merge(this_NonNegativeInteger_0);
                        
                     
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../com.b2international.snowowl.snomed.ecl/src-gen/com/b2international/snowowl/snomed/ecl/parser/antlr/internal/InternalEcl.g:1992:10: this_WILDCARD_1= RULE_WILDCARD
                    {
                    this_WILDCARD_1=(Token)match(input,RULE_WILDCARD,FOLLOW_RULE_WILDCARD_in_ruleMaxValue5056); 

                    		current.merge(this_WILDCARD_1);
                        
                     
                        newLeafNode(this_WILDCARD_1, grammarAccess.getMaxValueAccess().getWILDCARDTerminalRuleCall_1()); 
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "ruleMaxValue"

>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    // Delegated rules


 

    public static final BitSet FOLLOW_ruleExpressionConstraint_in_entryRuleExpressionConstraint81 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExpressionConstraint91 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleOrExpressionConstraint_in_ruleExpressionConstraint141 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleOrExpressionConstraint_in_entryRuleOrExpressionConstraint179 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleOrExpressionConstraint189 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAndExpressionConstraint_in_ruleOrExpressionConstraint236 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_RULE_OR_in_ruleOrExpressionConstraint256 = new BitSet(new long[]{0x0000000000A2FE00L});
    public static final BitSet FOLLOW_ruleAndExpressionConstraint_in_ruleOrExpressionConstraint276 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_ruleAndExpressionConstraint_in_entryRuleAndExpressionConstraint314 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleAndExpressionConstraint324 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExclusionExpressionConstraint_in_ruleAndExpressionConstraint371 = new BitSet(new long[]{0x0000000000000062L});
    public static final BitSet FOLLOW_RULE_AND_in_ruleAndExpressionConstraint392 = new BitSet(new long[]{0x0000000000A2FE00L});
    public static final BitSet FOLLOW_RULE_COMMA_in_ruleAndExpressionConstraint408 = new BitSet(new long[]{0x0000000000A2FE00L});
    public static final BitSet FOLLOW_ruleExclusionExpressionConstraint_in_ruleAndExpressionConstraint429 = new BitSet(new long[]{0x0000000000000062L});
    public static final BitSet FOLLOW_ruleExclusionExpressionConstraint_in_entryRuleExclusionExpressionConstraint467 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExclusionExpressionConstraint477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleRefinedExpressionConstraint_in_ruleExclusionExpressionConstraint524 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_RULE_MINUS_in_ruleExclusionExpressionConstraint544 = new BitSet(new long[]{0x0000000000A2FE00L});
    public static final BitSet FOLLOW_ruleRefinedExpressionConstraint_in_ruleExclusionExpressionConstraint564 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleRefinedExpressionConstraint_in_entryRuleRefinedExpressionConstraint602 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleRefinedExpressionConstraint612 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleSimpleExpressionConstraint_in_ruleRefinedExpressionConstraint659 = new BitSet(new long[]{0x0000000000000102L});
<<<<<<< feature/ecl-support
    public static final BitSet FOLLOW_RULE_COLON_in_ruleRefinedExpressionConstraint679 = new BitSet(new long[]{0x0000000000860000L});
=======
    public static final BitSet FOLLOW_RULE_COLON_in_ruleRefinedExpressionConstraint679 = new BitSet(new long[]{0x00000000040E0C00L});
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...
    public static final BitSet FOLLOW_ruleRefinement_in_ruleRefinedExpressionConstraint699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleSimpleExpressionConstraint_in_entryRuleSimpleExpressionConstraint737 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleSimpleExpressionConstraint747 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleChildOf_in_ruleSimpleExpressionConstraint794 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleDescendantOf_in_ruleSimpleExpressionConstraint821 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleDescendantOrSelfOf_in_ruleSimpleExpressionConstraint848 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleParentOf_in_ruleSimpleExpressionConstraint875 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAncestorOf_in_ruleSimpleExpressionConstraint902 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAncestorOrSelfOf_in_ruleSimpleExpressionConstraint929 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFocusConcept_in_ruleSimpleExpressionConstraint956 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFocusConcept_in_entryRuleFocusConcept991 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleFocusConcept1001 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleMemberOf_in_ruleFocusConcept1048 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleConceptReference_in_ruleFocusConcept1075 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAny_in_ruleFocusConcept1102 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNestedExpression_in_ruleFocusConcept1129 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleChildOf_in_entryRuleChildOf1164 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleChildOf1174 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_LT_EM_in_ruleChildOf1210 = new BitSet(new long[]{0x0000000000A2FE00L});
    public static final BitSet FOLLOW_ruleFocusConcept_in_ruleChildOf1230 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleDescendantOf_in_entryRuleDescendantOf1266 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleDescendantOf1276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_LT_in_ruleDescendantOf1312 = new BitSet(new long[]{0x0000000000A2FE00L});
    public static final BitSet FOLLOW_ruleFocusConcept_in_ruleDescendantOf1332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleDescendantOrSelfOf_in_entryRuleDescendantOrSelfOf1368 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleDescendantOrSelfOf1378 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_DBL_LT_in_ruleDescendantOrSelfOf1414 = new BitSet(new long[]{0x0000000000A2FE00L});
    public static final BitSet FOLLOW_ruleFocusConcept_in_ruleDescendantOrSelfOf1434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleParentOf_in_entryRuleParentOf1470 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleParentOf1480 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_GT_EM_in_ruleParentOf1516 = new BitSet(new long[]{0x0000000000A2FE00L});
    public static final BitSet FOLLOW_ruleFocusConcept_in_ruleParentOf1536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAncestorOf_in_entryRuleAncestorOf1572 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleAncestorOf1582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_GT_in_ruleAncestorOf1618 = new BitSet(new long[]{0x0000000000A2FE00L});
    public static final BitSet FOLLOW_ruleFocusConcept_in_ruleAncestorOf1638 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAncestorOrSelfOf_in_entryRuleAncestorOrSelfOf1674 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleAncestorOrSelfOf1684 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_DBL_GT_in_ruleAncestorOrSelfOf1720 = new BitSet(new long[]{0x0000000000A2FE00L});
    public static final BitSet FOLLOW_ruleFocusConcept_in_ruleAncestorOrSelfOf1740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleMemberOf_in_entryRuleMemberOf1776 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleMemberOf1786 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_CARET_in_ruleMemberOf1822 = new BitSet(new long[]{0x0000000000820000L});
    public static final BitSet FOLLOW_ruleConceptReference_in_ruleMemberOf1844 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAny_in_ruleMemberOf1863 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleConceptReference_in_entryRuleConceptReference1902 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleConceptReference1912 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleSnomedIdentifier_in_ruleConceptReference1958 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_RULE_PIPE_in_ruleConceptReference1970 = new BitSet(new long[]{0x00000007FDECFF70L});
    public static final BitSet FOLLOW_ruleTerm_in_ruleConceptReference1990 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RULE_PIPE_in_ruleConceptReference2001 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAny_in_entryRuleAny2038 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleAny2048 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_WILDCARD_in_ruleAny2084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleRefinement_in_entryRuleRefinement2128 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleRefinement2138 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAttributeConstraint_in_ruleRefinement2184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAttributeConstraint_in_entryRuleAttributeConstraint2218 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleAttributeConstraint2228 = new BitSet(new long[]{0x0000000000000002L});
<<<<<<< feature/ecl-support
    public static final BitSet FOLLOW_RULE_REVERSED_in_ruleAttributeConstraint2270 = new BitSet(new long[]{0x0000000000820000L});
    public static final BitSet FOLLOW_ruleConceptReference_in_ruleAttributeConstraint2299 = new BitSet(new long[]{0x0000000000180000L});
    public static final BitSet FOLLOW_ruleAny_in_ruleAttributeConstraint2318 = new BitSet(new long[]{0x0000000000180000L});
    public static final BitSet FOLLOW_ruleComparison_in_ruleAttributeConstraint2342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleComparison_in_entryRuleComparison2378 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleComparison2388 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAttributeValueEquals_in_ruleComparison2435 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAttributeValueNotEquals_in_ruleComparison2462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAttributeValueEquals_in_entryRuleAttributeValueEquals2497 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleAttributeValueEquals2507 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_EQUAL_in_ruleAttributeValueEquals2543 = new BitSet(new long[]{0x0000000000A2FE00L});
    public static final BitSet FOLLOW_ruleSimpleExpressionConstraint_in_ruleAttributeValueEquals2563 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAttributeValueNotEquals_in_entryRuleAttributeValueNotEquals2599 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleAttributeValueNotEquals2609 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_NOT_EQUAL_in_ruleAttributeValueNotEquals2645 = new BitSet(new long[]{0x0000000000A2FE00L});
    public static final BitSet FOLLOW_ruleSimpleExpressionConstraint_in_ruleAttributeValueNotEquals2665 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNestedExpression_in_entryRuleNestedExpression2701 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleNestedExpression2711 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ROUND_OPEN_in_ruleNestedExpression2747 = new BitSet(new long[]{0x0000000000A2FE00L});
    public static final BitSet FOLLOW_ruleExpressionConstraint_in_ruleNestedExpression2767 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_RULE_ROUND_CLOSE_in_ruleNestedExpression2778 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleSnomedIdentifier_in_entryRuleSnomedIdentifier2820 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleSnomedIdentifier2831 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier2875 = new BitSet(new long[]{0x0000000001800000L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier2896 = new BitSet(new long[]{0x0000000001800000L});
    public static final BitSet FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier2922 = new BitSet(new long[]{0x0000000001800000L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier2944 = new BitSet(new long[]{0x0000000001800000L});
    public static final BitSet FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier2970 = new BitSet(new long[]{0x0000000001800000L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier2992 = new BitSet(new long[]{0x0000000001800000L});
    public static final BitSet FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3018 = new BitSet(new long[]{0x0000000001800000L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3040 = new BitSet(new long[]{0x0000000001800000L});
    public static final BitSet FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3066 = new BitSet(new long[]{0x0000000001800000L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3088 = new BitSet(new long[]{0x0000000001800002L});
    public static final BitSet FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3114 = new BitSet(new long[]{0x0000000001800002L});
    public static final BitSet FOLLOW_ruleTerm_in_entryRuleTerm3172 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleTerm3183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTermCharacter_in_ruleTerm3235 = new BitSet(new long[]{0x00000007FFECFF72L});
    public static final BitSet FOLLOW_RULE_WS_in_ruleTerm3259 = new BitSet(new long[]{0x00000007FFECFF70L});
    public static final BitSet FOLLOW_ruleTermCharacter_in_ruleTerm3289 = new BitSet(new long[]{0x00000007FFECFF72L});
    public static final BitSet FOLLOW_ruleTermCharacter_in_entryRuleTermCharacter3349 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleTermCharacter3360 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_LT_in_ruleTermCharacter3404 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_GT_in_ruleTermCharacter3430 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_DBL_LT_in_ruleTermCharacter3456 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_DBL_GT_in_ruleTermCharacter3482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_LT_EM_in_ruleTermCharacter3508 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_GT_EM_in_ruleTermCharacter3534 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_AND_in_ruleTermCharacter3560 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_OR_in_ruleTermCharacter3586 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_NOT_in_ruleTermCharacter3612 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ZERO_in_ruleTermCharacter3638 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleTermCharacter3664 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_LETTER_in_ruleTermCharacter3690 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_CARET_in_ruleTermCharacter3716 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_EQUAL_in_ruleTermCharacter3742 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_PLUS_in_ruleTermCharacter3768 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_CURLY_OPEN_in_ruleTermCharacter3794 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_CURLY_CLOSE_in_ruleTermCharacter3820 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ROUND_OPEN_in_ruleTermCharacter3846 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ROUND_CLOSE_in_ruleTermCharacter3872 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_SQUARE_OPEN_in_ruleTermCharacter3898 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_SQUARE_CLOSE_in_ruleTermCharacter3924 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_DOT_in_ruleTermCharacter3950 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_COLON_in_ruleTermCharacter3976 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_COMMA_in_ruleTermCharacter4002 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_REVERSED_in_ruleTermCharacter4028 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_OTHER_CHARACTER_in_ruleTermCharacter4054 = new BitSet(new long[]{0x0000000000000002L});
=======
    public static final BitSet FOLLOW_ruleCardinality_in_ruleAttributeConstraint2274 = new BitSet(new long[]{0x00000000040E0C00L});
    public static final BitSet FOLLOW_RULE_REVERSED_in_ruleAttributeConstraint2292 = new BitSet(new long[]{0x00000000040E0C00L});
    public static final BitSet FOLLOW_ruleAttribute_in_ruleAttributeConstraint2319 = new BitSet(new long[]{0x0000000000C00000L});
    public static final BitSet FOLLOW_ruleComparison_in_ruleAttributeConstraint2340 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAttribute_in_entryRuleAttribute2376 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleAttribute2386 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAttributeDescendantOf_in_ruleAttribute2433 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAttributeDescendantOrSelfOf_in_ruleAttribute2460 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleConceptReference_in_ruleAttribute2487 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAny_in_ruleAttribute2514 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAttributeDescendantOf_in_entryRuleAttributeDescendantOf2549 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleAttributeDescendantOf2559 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_LT_in_ruleAttributeDescendantOf2595 = new BitSet(new long[]{0x0000000004020000L});
    public static final BitSet FOLLOW_ruleConceptReference_in_ruleAttributeDescendantOf2617 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAny_in_ruleAttributeDescendantOf2636 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAttributeDescendantOrSelfOf_in_entryRuleAttributeDescendantOrSelfOf2675 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleAttributeDescendantOrSelfOf2685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_DBL_LT_in_ruleAttributeDescendantOrSelfOf2721 = new BitSet(new long[]{0x0000000004020000L});
    public static final BitSet FOLLOW_ruleConceptReference_in_ruleAttributeDescendantOrSelfOf2743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAny_in_ruleAttributeDescendantOrSelfOf2762 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleCardinality_in_entryRuleCardinality2801 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleCardinality2811 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_SQUARE_OPEN_in_ruleCardinality2847 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_ruleNonNegativeInteger_in_ruleCardinality2867 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_RULE_TO_in_ruleCardinality2878 = new BitSet(new long[]{0x000000000C020000L});
    public static final BitSet FOLLOW_ruleMaxValue_in_ruleCardinality2898 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RULE_SQUARE_CLOSE_in_ruleCardinality2909 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleComparison_in_entryRuleComparison2944 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleComparison2954 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAttributeValueEquals_in_ruleComparison3001 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAttributeValueNotEquals_in_ruleComparison3028 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAttributeValueEquals_in_entryRuleAttributeValueEquals3063 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleAttributeValueEquals3073 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_EQUAL_in_ruleAttributeValueEquals3109 = new BitSet(new long[]{0x000000000502FE00L});
    public static final BitSet FOLLOW_ruleSimpleExpressionConstraint_in_ruleAttributeValueEquals3129 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAttributeValueNotEquals_in_entryRuleAttributeValueNotEquals3165 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleAttributeValueNotEquals3175 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_NOT_EQUAL_in_ruleAttributeValueNotEquals3211 = new BitSet(new long[]{0x000000000502FE00L});
    public static final BitSet FOLLOW_ruleSimpleExpressionConstraint_in_ruleAttributeValueNotEquals3231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNestedExpression_in_entryRuleNestedExpression3267 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleNestedExpression3277 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ROUND_OPEN_in_ruleNestedExpression3313 = new BitSet(new long[]{0x000000000502FE00L});
    public static final BitSet FOLLOW_ruleExpressionConstraint_in_ruleNestedExpression3333 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_RULE_ROUND_CLOSE_in_ruleNestedExpression3344 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleSnomedIdentifier_in_entryRuleSnomedIdentifier3386 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleSnomedIdentifier3397 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3441 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3462 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3488 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3510 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3536 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3558 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3584 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3606 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3632 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleSnomedIdentifier3654 = new BitSet(new long[]{0x000000000C000002L});
    public static final BitSet FOLLOW_RULE_ZERO_in_ruleSnomedIdentifier3680 = new BitSet(new long[]{0x000000000C000002L});
    public static final BitSet FOLLOW_ruleTerm_in_entryRuleTerm3738 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleTerm3749 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTermCharacter_in_ruleTerm3801 = new BitSet(new long[]{0x0000000FFFFEFFF2L});
    public static final BitSet FOLLOW_RULE_WS_in_ruleTerm3825 = new BitSet(new long[]{0x0000000FFFFEFFF0L});
    public static final BitSet FOLLOW_ruleTermCharacter_in_ruleTerm3855 = new BitSet(new long[]{0x0000000FFFFEFFF2L});
    public static final BitSet FOLLOW_ruleTermCharacter_in_entryRuleTermCharacter3915 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleTermCharacter3926 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_LT_in_ruleTermCharacter3970 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_GT_in_ruleTermCharacter3996 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_DBL_LT_in_ruleTermCharacter4022 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_DBL_GT_in_ruleTermCharacter4048 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_LT_EM_in_ruleTermCharacter4074 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_GT_EM_in_ruleTermCharacter4100 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_AND_in_ruleTermCharacter4126 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_OR_in_ruleTermCharacter4152 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_NOT_in_ruleTermCharacter4178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_MINUS_in_ruleTermCharacter4204 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ZERO_in_ruleTermCharacter4230 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleTermCharacter4256 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_LETTER_in_ruleTermCharacter4282 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_CARET_in_ruleTermCharacter4308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_EQUAL_in_ruleTermCharacter4334 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_NOT_EQUAL_in_ruleTermCharacter4360 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_PLUS_in_ruleTermCharacter4386 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_CURLY_OPEN_in_ruleTermCharacter4412 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_CURLY_CLOSE_in_ruleTermCharacter4438 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ROUND_OPEN_in_ruleTermCharacter4464 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ROUND_CLOSE_in_ruleTermCharacter4490 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_SQUARE_OPEN_in_ruleTermCharacter4516 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_SQUARE_CLOSE_in_ruleTermCharacter4542 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_DOT_in_ruleTermCharacter4568 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_COLON_in_ruleTermCharacter4594 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_COMMA_in_ruleTermCharacter4620 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_REVERSED_in_ruleTermCharacter4646 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_TO_in_ruleTermCharacter4672 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_WILDCARD_in_ruleTermCharacter4698 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_OTHER_CHARACTER_in_ruleTermCharacter4724 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNonNegativeInteger_in_entryRuleNonNegativeInteger4780 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleNonNegativeInteger4791 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ZERO_in_ruleNonNegativeInteger4835 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleNonNegativeInteger4862 = new BitSet(new long[]{0x000000000C000002L});
    public static final BitSet FOLLOW_RULE_DIGIT_NONZERO_in_ruleNonNegativeInteger4883 = new BitSet(new long[]{0x000000000C000002L});
    public static final BitSet FOLLOW_RULE_ZERO_in_ruleNonNegativeInteger4909 = new BitSet(new long[]{0x000000000C000002L});
    public static final BitSet FOLLOW_ruleMaxValue_in_entryRuleMaxValue4968 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleMaxValue4979 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNonNegativeInteger_in_ruleMaxValue5030 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_WILDCARD_in_ruleMaxValue5056 = new BitSet(new long[]{0x0000000000000002L});
>>>>>>> be9d8b9 SO-1782: support descendantOf and desc.OrSelfOf in attribute part...

}