package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Perm;
import com.massivecraft.massivecore.command.requirement.RequirementHasPerm;

public class CmdFactionsUnclaim extends FactionsCommand {
	// -------------------------------------------- //
	// FIELDS
	// -------------------------------------------- //

	public CmdFactionsSetOne cmdFactionsUnclaimOne = new CmdFactionsSetOne(
			false);
	public CmdFactionsSetLine cmdFactionsClaimLine = new CmdFactionsSetLine(
			false);
	public CmdFactionsSetAuto cmdFactionsUnclaimAuto = new CmdFactionsSetAuto(
			false);
	public CmdFactionsSetFill cmdFactionsUnclaimFill = new CmdFactionsSetFill(
			false);
	public CmdFactionsSetSquare cmdFactionsUnclaimSquare = new CmdFactionsSetSquare(
			false);
	public CmdFactionsSetCircle cmdFactionsUnclaimCircle = new CmdFactionsSetCircle(
			false);
	public CmdFactionsSetAll cmdFactionsUnclaimAll = new CmdFactionsSetAll(
			false);
	public CmdFactionsSetChunk cmdFactionsSetChunk = new CmdFactionsSetChunk(
			false);
	public CmdFactionsUnclaimMap cmdFactionsUnclaimMap = new CmdFactionsUnclaimMap();

	// -------------------------------------------- //
	// CONSTRUCT
	// -------------------------------------------- //

	public CmdFactionsUnclaim() {
		// Aliases
		this.addAliases("unclaim");

		// Children
		this.addChild(this.cmdFactionsUnclaimOne);
		this.addChild(this.cmdFactionsClaimLine);
		this.addChild(this.cmdFactionsUnclaimAuto);
		this.addChild(this.cmdFactionsUnclaimFill);
		this.addChild(this.cmdFactionsUnclaimSquare);
		this.addChild(this.cmdFactionsUnclaimCircle);
		this.addChild(this.cmdFactionsUnclaimAll);
		this.addChild(this.cmdFactionsSetChunk);
		this.addChild(this.cmdFactionsUnclaimMap);

		// Requirements
		this.addRequirements(RequirementHasPerm.get(Perm.UNCLAIM.node));
	}

}
